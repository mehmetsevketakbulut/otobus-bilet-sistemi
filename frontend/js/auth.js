document.addEventListener('DOMContentLoaded', () => {
    const loginForm = document.getElementById('loginForm');
    const registerForm = document.getElementById('registerForm');
    const alertBox = document.getElementById('authAlert');

    function showAlert(message, type = 'error') {
        if (!alertBox) return;
        alertBox.textContent = message;
        alertBox.className = `form-alert ${type}`;
        alertBox.style.display = 'block';
    }

    if (loginForm) {
        loginForm.addEventListener('submit', async (e) => {
            e.preventDefault();
            const email = document.getElementById('email').value;
            const password = document.getElementById('password').value;
            const btn = loginForm.querySelector('button');

            try {
                btn.disabled = true;
                btn.textContent = 'Giriş Yapılıyor...';
                
                const response = await api.auth.login({ email, password });
                
                // Save token
                setToken(response.token);
                showAlert('Giriş başarılı! Yönlendiriliyorsunuz...', 'success');
                
                const role = getUserRole();
                setTimeout(() => {
                    if (role === 'ADMIN') window.location.href = 'admin.html';
                    else if (role === 'COMPANY') window.location.href = 'company.html';
                    else window.location.href = 'index.html';
                }, 1000);
            } catch (error) {
                showAlert(error.message || 'Hatalı e-posta veya şifre.');
                btn.disabled = false;
                btn.textContent = 'Giriş Yap';
            }
        });
    }

    if (registerForm) {
        registerForm.addEventListener('submit', async (e) => {
            e.preventDefault();
            const fullName = document.getElementById('fullName').value;
            const email = document.getElementById('email').value;
            const phoneNumber = document.getElementById('phoneNumber').value;
            const role = document.getElementById('role').value;
            const password = document.getElementById('password').value;
            const confirmPassword = document.getElementById('confirmPassword').value;
            const btn = registerForm.querySelector('button');

            if (password !== confirmPassword) {
                showAlert('Şifreler eşleşmiyor!');
                return;
            }

            try {
                btn.disabled = true;
                btn.textContent = 'Kayıt Olunuyor...';
                
                const response = await api.auth.register({ fullName, email, phoneNumber, password, role });
                
                setToken(response.token);
                showAlert('Kayıt başarılı! Yönlendiriliyorsunuz...', 'success');
                
                const userRole = getUserRole();
                setTimeout(() => {
                    if (userRole === 'ADMIN') window.location.href = 'admin.html';
                    else if (userRole === 'COMPANY') window.location.href = 'company.html';
                    else window.location.href = 'index.html';
                }, 1000);
            } catch (error) {
                showAlert(error.message || 'Kayıt olurken bir hata oluştu.');
                btn.disabled = false;
                btn.textContent = 'Kayıt Ol';
            }
        });
    }
});
