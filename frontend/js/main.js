document.addEventListener('DOMContentLoaded', () => {
    updateNavUI();

    const logoutBtn = document.getElementById('logoutBtn');
    if (logoutBtn) {
        logoutBtn.addEventListener('click', (e) => {
            e.preventDefault();
            clearToken();
            updateNavUI();
            window.location.reload();
        });
    }
});

function updateNavUI() {
    const loginLink = document.getElementById('navLogin');
    const registerLink = document.getElementById('navRegister');
    const logoutLink = document.getElementById('navLogout');
    const userProfileLink = document.getElementById('navProfile');

    if (isLoggedIn()) {
        if (loginLink) loginLink.style.display = 'none';
        if (registerLink) registerLink.style.display = 'none';
        if (logoutLink) logoutLink.style.display = 'block';
        if (userProfileLink) userProfileLink.style.display = 'block';
    } else {
        if (loginLink) loginLink.style.display = 'block';
        if (registerLink) registerLink.style.display = 'block';
        if (logoutLink) logoutLink.style.display = 'none';
        if (userProfileLink) userProfileLink.style.display = 'none';
    }
}
