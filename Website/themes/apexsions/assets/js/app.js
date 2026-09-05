document.addEventListener('DOMContentLoaded', () => {
    // Clipboard copy for Server IP
    const ipWidget = document.getElementById('apxIpWidget');
    if (ipWidget) {
        ipWidget.addEventListener('click', () => {
            const ip = ipWidget.getAttribute('data-ip') || 'apexsions.my.id';
            navigator.clipboard.writeText(ip).then(() => {
                const btn = ipWidget.querySelector('.btn');
                const originalHtml = btn.innerHTML;
                btn.innerHTML = '<i class="bi bi-check2"></i> Disalin!';
                btn.classList.remove('btn-apx-primary');
                btn.classList.add('btn-success');

                setTimeout(() => {
                    btn.innerHTML = originalHtml;
                    btn.classList.remove('btn-success');
                    btn.classList.add('btn-apx-primary');
                }, 2500);
            }).catch(err => {
                console.error('Failed to copy IP to clipboard:', err);
            });
        });
    }

    // Live Server Ping Counter
    const playerCountEl = document.getElementById('apxPlayerCount');
    if (playerCountEl) {
        const serverAddress = playerCountEl.getAttribute('data-server') || 'apexsions.my.id';
        fetch(`https://api.mcsrvstat.us/3/${encodeURIComponent(serverAddress)}`)
            .then(res => res.json())
            .then(data => {
                if (data.online) {
                    playerCountEl.textContent = `${data.players.online} Pemain Online`;
                } else {
                    playerCountEl.textContent = 'Server Online';
                }
            })
            .catch(() => {
                playerCountEl.textContent = 'Server Online';
            });
    }
});
