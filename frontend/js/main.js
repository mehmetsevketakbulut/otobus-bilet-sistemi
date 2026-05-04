document.addEventListener('DOMContentLoaded', () => {
    updateNavUI();

    // Çıkış Yap butonu (navbar'daki id="navLogout")
    const logoutBtn = document.getElementById('navLogout');
    if (logoutBtn) {
        logoutBtn.addEventListener('click', (e) => {
            e.preventDefault();
            // Güvenlik: tüm oturum verilerini temizle
            clearToken();
            localStorage.removeItem('jwt_token');
            sessionStorage.clear();
            updateNavUI();
            window.location.href = 'index.html';
        });
    }

    // Profilim butonu
    const profileBtn = document.getElementById('navProfile');
    if (profileBtn) {
        profileBtn.addEventListener('click', (e) => {
            e.preventDefault();
            window.location.href = 'profile.html';
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
