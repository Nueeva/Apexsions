package com.apexsions.media.testutil;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.stream.Stream;

/**
 * Implementasi {@link RegistryAccess} khusus unit-test.
 *
 * <p><b>Mengapa ada:</b> konstanta seperti {@code org.bukkit.Sound} diinisialisasi
 * lewat Paper registry saat class dimuat. Tanpa server, inisialisasi itu melempar
 * {@code ExceptionInInitializerError}. MockBukkit tidak bisa dipakai karena versi
 * terakhir untuk lini 1.21 (3.133.2) dibangun untuk paper-api 1.21.1 dan gagal
 * bootstrap di paper-api 1.21.4 (key registry baru + sirkularitas inisialisasi
 * {@code Registry.ART}). Kelas ini menyediakan registry kosong yang lenien agar
 * konstanta registry terinisialisasi sebagai {@code null} tanpa exception.</p>
 *
 * <p>Terdaftar via {@code META-INF/services} agar {@code RegistryAccess.registryAccess()}
 * menemukannya lewat {@link java.util.ServiceLoader}.</p>
 *
 * <p><b>Hanya untuk test.</b> Jangan dipakai di production — {@code getOrThrow}
 * yang mengembalikan {@code null} melanggar kontrak API aslinya.</p>
 */
public final class LenientTestRegistryAccess implements RegistryAccess {

    @Override
    public <T extends Keyed> @Nullable Registry<T> getRegistry(Class<T> type) {
        return new LenientRegistry<>();
    }

    @Override
    public <T extends Keyed> @NotNull Registry<T> getRegistry(RegistryKey<T> registryKey) {
        return new LenientRegistry<>();
    }

    private static final class LenientRegistry<T extends Keyed> implements Registry<T> {

        @Override
        public @Nullable T get(@NotNull NamespacedKey key) {
            return null;
        }

        @Override
        public @Nullable NamespacedKey getKey(@NotNull T value) {
            return null;
        }

        @Override
        public @NotNull Iterator<T> iterator() {
            return Collections.emptyIterator();
        }

        @Override
        public @NotNull Stream<T> stream() {
            return Stream.empty();
        }

        @Override
        public int size() {
            return 0;
        }

        @Override
        public boolean hasTag(io.papermc.paper.registry.tag.TagKey<T> key) {
            return false;
        }

        @Override
        public io.papermc.paper.registry.tag.Tag<T> getTag(io.papermc.paper.registry.tag.TagKey<T> key) {
            return null;
        }

        @Override
        public @NotNull Collection<io.papermc.paper.registry.tag.Tag<T>> getTags() {
            return Collections.emptyList();
        }

        /**
         * Lenien: kembalikan {@code null} alih-alih melempar, agar static
         * initializer seperti {@code Sound.<clinit>} tidak gagal di test.
         */
        @Override
        @SuppressWarnings("NullableProblems")
        public T getOrThrow(@NotNull NamespacedKey key) {
            return null;
        }
    }
}
