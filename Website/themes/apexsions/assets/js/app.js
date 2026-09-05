document.addEventListener('DOMContentLoaded', () => {
    // 1. One-Click Copy for Server Address & Port with Visual Feedback
    const copyElements = document.querySelectorAll('[data-apx-copy]');
    copyElements.forEach(el => {
        const handleCopy = (e) => {
            if (e) {
                e.preventDefault();
                e.stopPropagation();
            }
            const textToCopy = el.getAttribute('data-apx-copy') || 'apexsions.my.id';
            navigator.clipboard.writeText(textToCopy).then(() => {
                const badgeEl = el.querySelector('.badge-copy');
                const originalHtml = badgeEl ? badgeEl.innerHTML : el.innerHTML;

                if (badgeEl) {
                    badgeEl.innerHTML = '<i class="bi bi-check-lg text-success"></i>';
                    setTimeout(() => {
                        badgeEl.innerHTML = originalHtml;
                    }, 2000);
                } else {
                    const originalBtnContent = el.innerHTML;
                    el.innerHTML = '<i class="bi bi-check-lg me-1 text-success"></i> Disalin!';
                    el.classList.add('border-success');

                    setTimeout(() => {
                        el.innerHTML = originalBtnContent;
                        el.classList.remove('border-success');
                    }, 2000);
                }
            }).catch(err => {
                console.warn('Clipboard write failed:', err);
            });
        };

        el.addEventListener('click', handleCopy);
        el.addEventListener('keydown', (e) => {
            if (e.key === 'Enter' || e.key === ' ') {
                e.preventDefault();
                handleCopy(e);
            }
        });
    });

    // 2. Live Minecraft Server Bridge Integration
    const fetchServerStatus = () => {
        const playersEl = document.getElementById('apxOnlinePlayers');
        const footerPlayersEl = document.getElementById('apxFooterPlayers');
        const footerMaxPlayersEl = document.getElementById('apxFooterMaxPlayers');
        const footerVersionEl = document.getElementById('apxFooterVersion');
        const footerBadgeEl = document.getElementById('apxFooterStatusBadge');
        const footerHeadingDotEl = document.getElementById('apxFooterHeadingDot');
        const maxPlayersEl = document.getElementById('apxMaxPlayers');
        const versionEl = document.getElementById('apxVersion');
        const liveBadgeEl = document.getElementById('apxLiveBadge');
        const liveSubEl = document.getElementById('apxLiveSub');
        const liveDotEl = document.getElementById('apxLiveDot');

        const applyStatus = (online, players, maxPlayers, version) => {
            if (playersEl) playersEl.textContent = players ?? 0;
            if (footerPlayersEl) footerPlayersEl.textContent = players ?? 0;
            if (maxPlayersEl) maxPlayersEl.textContent = maxPlayers ?? 500;
            if (footerMaxPlayersEl) footerMaxPlayersEl.textContent = maxPlayers ?? 500;
            if (versionEl && version) versionEl.textContent = version;
            if (footerVersionEl && version) footerVersionEl.textContent = version;

            if (online) {
                if (liveBadgeEl) liveBadgeEl.textContent = 'SERVER ONLINE';
                if (liveSubEl) liveSubEl.textContent = 'Java & Bedrock Siap';
                if (liveDotEl) {
                    liveDotEl.style.background = '#10b981';
                    liveDotEl.style.boxShadow = '0 0 10px #10b981';
                }
                if (footerBadgeEl) {
                    footerBadgeEl.className = 'apx-status-pill apx-pill-online flex-shrink-0 ms-2';
                    footerBadgeEl.innerHTML = '<span class="apx-pulse-dot-sm"></span> ONLINE';
                }
                if (footerHeadingDotEl) {
                    footerHeadingDotEl.style.background = '#10b981';
                    footerHeadingDotEl.style.boxShadow = '0 0 8px #10b981';
                }
            } else {
                if (liveBadgeEl) liveBadgeEl.textContent = 'SERVER OFFLINE';
                if (liveSubEl) liveSubEl.textContent = 'Sedang Pemeliharaan';
                if (liveDotEl) {
                    liveDotEl.style.background = '#ef4444';
                    liveDotEl.style.boxShadow = '0 0 10px #ef4444';
                }
                if (footerBadgeEl) {
                    footerBadgeEl.className = 'apx-status-pill apx-pill-offline flex-shrink-0 ms-2';
                    footerBadgeEl.innerHTML = 'OFFLINE';
                }
                if (footerHeadingDotEl) {
                    footerHeadingDotEl.style.background = '#ef4444';
                    footerHeadingDotEl.style.boxShadow = 'none';
                }
            }
        };

        fetch('/api/apexsions-bridge/status')
            .then(res => res.json())
            .then(data => {
                if (data && typeof data.online !== 'undefined') {
                    applyStatus(data.online, data.players, data.max_players, data.version);
                } else {
                    // Fallback to mcstatus public lookup
                    fetch('https://api.mcstatus.io/v2/status/java/apexsions.my.id')
                        .then(res => res.json())
                        .then(mcData => {
                            if (mcData && mcData.online) {
                                applyStatus(true, mcData.players?.online ?? 0, mcData.players?.max ?? 500, mcData.version?.name_clean ?? '1.21.4');
                            } else {
                                applyStatus(false, 0, 500, '1.21.4');
                            }
                        })
                        .catch(() => {
                            applyStatus(true, 0, 500, '1.21.4');
                        });
                }
            })
            .catch(() => {
                // Graceful default
                applyStatus(true, 0, 500, '1.21.4');
            });
    };

    fetchServerStatus();
    // Poll every 30 seconds for live updates
    setInterval(fetchServerStatus, 30000);

    // 3. Interactive Rank Hierarchy Filter
    const rankPills = document.querySelectorAll('[data-rank-filter]');
    const rankCards = document.querySelectorAll('[data-rank-category]');
    rankPills.forEach(pill => {
        pill.addEventListener('click', () => {
            rankPills.forEach(p => {
                p.classList.remove('active');
                p.setAttribute('aria-selected', 'false');
            });
            pill.classList.add('active');
            pill.setAttribute('aria-selected', 'true');

            const filter = pill.getAttribute('data-rank-filter');
            rankCards.forEach(card => {
                if (filter === 'all' || card.getAttribute('data-rank-category') === filter) {
                    card.style.display = '';
                } else {
                    card.style.display = 'none';
                }
            });
        });
    });
});
