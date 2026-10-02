package com.apexsions.economy.service;

import com.apexsions.economy.ApexsionsEconomy;
import com.apexsions.economy.bank.BankDeposit;
import com.apexsions.economy.database.EconomyRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * T-1 — Regression test untuk C-1 (race condition double-claim deposito → duplikasi uang).
 *
 * <p>Menjalankan komposisi yang dipakai {@code BankDepositService.claimDeposit} setelah
 * perbaikan, memakai kode produksi asli:</p>
 * <ul>
 *   <li>{@code EconomyRepository.claimBankDeposit} sungguhan di atas SQLite nyata
 *       (hanya {@code ApexsionsEconomy} yang di-mock untuk {@code getDataFolder()}/{@code getLogger()});</li>
 *   <li>{@code TransactionLockManager.executeWithResourceLock(depositId, ...)} sungguhan —
 *       lock per deposit-id yang sama dipakai service;</li>
 *   <li>{@code AtomicInteger} sebagai penghitung payout, mewakili
 *       {@code CurrencyService.addBalance(...)} (payout dieksekusi hanya jika klaim menang).</li>
 * </ul>
 *
 * <p>Dengan kode lama test ini GAGAL:</p>
 * <ul>
 *   <li>Signature lama {@code CompletableFuture<Void>} membuat test ini bahkan tidak bisa
 *       dikompilasi (tidak ada nilai boolean kemenangan klaim untuk di-assert).</li>
 *   <li>Jika guard {@code AND claimed = 0} dihapus (signature baru dipertahankan), kedua
 *       thread sama-sama mendapat {@code executeUpdate() == 1} → payout 2x → assertion gagal.</li>
 * </ul>
 */
public class BankDepositClaimRaceTest {

    private static final int RACERS = 16;

    @TempDir
    File dataFolder;

    private EconomyRepository repository;
    private TransactionLockManager lockManager;

    @BeforeEach
    void setUp() {
        ApexsionsEconomy plugin = mock(ApexsionsEconomy.class);
        when(plugin.getDataFolder()).thenReturn(dataFolder);
        when(plugin.getLogger()).thenReturn(Logger.getLogger(BankDepositClaimRaceTest.class.getName()));

        repository = new EconomyRepository(plugin, "test-economy.db");
        repository.init();
        lockManager = new TransactionLockManager();
    }

    @AfterEach
    void tearDown() {
        repository.close();
    }

    @Test
    @DisplayName("T-1: N thread claim deposito yang sama secara konkuren -> payout tepat 1x")
    void concurrentClaimsOfSameDepositPayOutExactlyOnce() throws Exception {
        String depositId = "race-" + UUID.randomUUID().toString().substring(0, 8);
        UUID owner = UUID.randomUUID();
        long now = System.currentTimeMillis();
        BankDeposit deposit = new BankDeposit(depositId, owner, "rupiah",
                1_000_000.0, 0.05, 1_050_000.0,
                now - 86_400_000L, now - 1_000L, false);
        repository.saveBankDeposit(deposit).join();

        // Sanity: deposito tersimpan dan terbaca kembali sebagai belum diklaim.
        assertEquals(1, repository.loadActiveBankDeposits(owner).join().size(),
                "setup: deposito harus tersimpan sebelum race dimulai");

        AtomicInteger payouts = new AtomicInteger(0);
        CountDownLatch startGate = new CountDownLatch(1);
        List<Thread> racers = new ArrayList<>();
        List<Throwable> errors = Collections.synchronizedList(new ArrayList<>());

        for (int i = 0; i < RACERS; i++) {
            Thread t = new Thread(() -> {
                try {
                    assertTrue(startGate.await(10, TimeUnit.SECONDS), "start gate timeout");
                    // Komposisi persis seperti BankDepositService.claimDeposit (post-fix):
                    // cek+payout dalam lock per deposit-id; payout hanya jika klaim atomik menang.
                    lockManager.executeWithResourceLock(depositId, () -> {
                        boolean claimedNow = repository.claimBankDeposit(depositId).join();
                        if (claimedNow) {
                            payouts.incrementAndGet(); // mewakili CurrencyService.addBalance(...)
                        }
                    });
                } catch (Throwable e) {
                    errors.add(e);
                }
            }, "deposit-racer-" + i);
            racers.add(t);
            t.start();
        }

        startGate.countDown();
        for (Thread t : racers) {
            t.join(30_000L);
            assertFalse(t.isAlive(), "racer thread hang: " + t.getName());
        }
        assertTrue(errors.isEmpty(), "racer errors: " + errors);

        assertEquals(1, payouts.get(),
                "C-1 REGRESSION: double-claim! payout dieksekusi " + payouts.get() + "x untuk satu deposito");

        // Klaim ulang setelah ada pemenang harus selalu gagal (idempoten).
        assertFalse(repository.claimBankDeposit(depositId).join(),
                "deposito yang sudah diklaim tidak boleh bisa diklaim lagi");
    }
}
