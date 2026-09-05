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

    // 4. Cinematic Inter-Page Transition (VolteraMC / AAA Portal Experience)
    const initPageTransitions = () => {
        const overlay = document.getElementById('apxPageTransition');
        if (!overlay) return;

        const dismissTransition = () => {
            overlay.classList.remove('is-navigating', 'is-entering');
            overlay.classList.add('is-loaded');
        };

        // Smoothly dismiss entrance overlay
        requestAnimationFrame(() => {
            setTimeout(dismissTransition, 80);
        });

        // Ensure browser history (Back / Forward bfcache) never leaves screen blocked
        window.addEventListener('pageshow', () => {
            dismissTransition();
        });

        // Instant Hover Prefetching for Internal Pages (eliminates loading lag)
        document.addEventListener('mouseover', (e) => {
            const link = e.target.closest('a');
            if (!link || !link.href) return;
            if (link.origin !== window.location.origin) return;
            if (link.hasAttribute('data-prefetched')) return;

            link.setAttribute('data-prefetched', 'true');
            const prefetchLink = document.createElement('link');
            prefetchLink.rel = 'prefetch';
            prefetchLink.href = link.href;
            document.head.appendChild(prefetchLink);
        }, { passive: true });

        // Intercept internal page navigation links
        document.addEventListener('click', (e) => {
            const link = e.target.closest('a');
            if (!link) return;

            const href = link.getAttribute('href');
            if (!href) return;

            // Ignore anchor jumps, javascript calls, mailto, tel
            if (href.startsWith('#') || href.startsWith('javascript:') || href.startsWith('mailto:') || href.startsWith('tel:')) {
                return;
            }

            // Ignore new-tab links, modifier keys, downloads, or modal triggers
            if (link.target === '_blank' || e.ctrlKey || e.metaKey || e.shiftKey || e.altKey) {
                return;
            }
            if (link.hasAttribute('data-bs-toggle') || link.hasAttribute('data-apx-copy') || link.hasAttribute('download')) {
                return;
            }

            try {
                const targetUrl = new URL(link.href, window.location.origin);
                // Only handle same-origin navigation
                if (targetUrl.origin !== window.location.origin) {
                    return;
                }

                // If clicking anchor on the current page, let browser handle smoothly
                if (targetUrl.pathname === window.location.pathname && targetUrl.search === window.location.search && targetUrl.hash) {
                    return;
                }

                // If identical URL, skip
                if (targetUrl.href === window.location.href) {
                    return;
                }

                // Trigger cinematic loading transition
                e.preventDefault();
                overlay.classList.remove('is-loaded');
                overlay.classList.add('is-navigating');

                // Snappy 200ms engagement before navigation
                setTimeout(() => {
                    window.location.href = targetUrl.href;
                }, 200);

                // Safety fallback
                setTimeout(() => {
                    dismissTransition();
                }, 2000);
            } catch (err) {
                // Ignore parse errors, let browser navigate normally
            }
        });
    };

    initPageTransitions();

    // 5. Cinematic Scroll Reveal Animations (Silky Smooth, Zero Popping)
    const initScrollAnimations = () => {
        if (window.matchMedia('(prefers-reduced-motion: reduce)').matches) {
            return;
        }

        const autoTargets = [
            '.apx-section-header',
            '.apx-pillar-monolith',
            '.apx-caste-card',
            '.apx-caste-banner',
            '.apx-step-monolith',
            '.apx-showcase-card',
            '.apx-rule-card',
            '.apx-rule-item',
            '.apx-wiki-category-card',
            '.apx-wiki-article-item',
            '.card',
            '.apx-scroll-reveal'
        ];

        const elements = document.querySelectorAll(autoTargets.join(', '));
        if (!elements.length) return;

        if (!('IntersectionObserver' in window)) {
            elements.forEach(el => el.classList.add('is-revealed'));
            return;
        }

        const observer = new IntersectionObserver((entries, obs) => {
            entries.forEach(entry => {
                if (entry.isIntersecting) {
                    entry.target.classList.add('is-revealed');
                    obs.unobserve(entry.target);
                }
            });
        }, {
            threshold: 0.05,
            rootMargin: '0px 0px -20px 0px'
        });

        const winHeight = window.innerHeight;
        elements.forEach(el => {
            const rect = el.getBoundingClientRect();
            // If already inside or just above the viewport on initial paint, reveal immediately without pop-in!
            if (rect.top < winHeight * 0.92) {
                el.classList.add('is-revealed');
            } else {
                // Below the fold: animate gracefully as user scrolls
                el.classList.add('apx-scroll-reveal');

                const parent = el.parentElement;
                if (parent && (parent.classList.contains('row') || parent.classList.contains('apx-stepper-grid') || parent.classList.contains('apx-pillar-grid'))) {
                    const childIndex = Array.from(parent.children).indexOf(el);
                    if (childIndex >= 0 && childIndex < 4) {
                        el.classList.add(`apx-reveal-stagger-${childIndex + 1}`);
                    }
                }

                observer.observe(el);
            }
        });
    };

    initScrollAnimations();
});
