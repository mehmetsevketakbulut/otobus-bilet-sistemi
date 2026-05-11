// Production: Nginx proxy üzerinden /api kullanır
// Lokal geliştirme: doğrudan backend'e bağlanır
const API_BASE_URL = window.location.hostname === 'localhost' || window.location.hostname === '127.0.0.1'
    ? 'http://localhost:8080/api'
    : '/api';

/**
 * Gets the stored JWT token
 */
function getToken() {
    return localStorage.getItem('jwt_token');
}

/**
 * Sets the JWT token
 */
function setToken(token) {
    localStorage.setItem('jwt_token', token);
}

/**
 * Clears the JWT token (Logout)
 */
function clearToken() {
    localStorage.removeItem('jwt_token');
}

/**
 * Checks if user is logged in
 */
function isLoggedIn() {
    return !!getToken();
}

/**
 * Decodes JWT Token to get claims
 */
function decodeToken(token) {
    if (!token) return null;
    try {
        const base64Url = token.split('.')[1];
        const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
        const jsonPayload = decodeURIComponent(atob(base64).split('').map(function(c) {
            return '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2);
        }).join(''));
        return JSON.parse(jsonPayload);
    } catch(e) {
        return null;
    }
}

/**
 * Gets user role from token
 */
function getUserRole() {
    const token = getToken();
    const decoded = decodeToken(token);
    return decoded ? decoded.role : null;
}

/**
 * Checks if user's email is verified from token
 */
function isEmailVerified() {
    const token = getToken();
    const decoded = decodeToken(token);
    return decoded ? decoded.emailVerified === true : false;
}

/**
 * Gets user email from token
 */
function getUserEmail() {
    const token = getToken();
    const decoded = decodeToken(token);
    return decoded ? decoded.sub : null;
}

/**
 * Base fetch wrapper that automatically adds Authorization header
 */
async function fetchApi(endpoint, options = {}) {
    const url = `${API_BASE_URL}${endpoint}`;
    
    const headers = {
        'Content-Type': 'application/json',
        ...options.headers
    };

    const token = getToken();
    if (token) {
        headers['Authorization'] = `Bearer ${token}`;
    }

    const config = {
        ...options,
        headers
    };

    try {
        const response = await fetch(url, config);

        let data;
        const contentType = response.headers.get('content-type');
        if (contentType && contentType.includes('application/json')) {
            data = await response.json();
        } else {
            const text = await response.text();
            data = { message: text || 'Bir hata oluştu!' };
        }
        
        if (!response.ok) {
            throw new Error(data.message || 'Bir hata oluştu!');
        }
        
        return data;
    } catch (error) {
        throw error;
    }
}

/**
 * Auth API methods
 */
const api = {
    auth: {
        login: (credentials) => fetchApi('/auth/login', {
            method: 'POST',
            body: JSON.stringify(credentials)
        }),
        register: (userData) => fetchApi('/auth/register', {
            method: 'POST',
            body: JSON.stringify(userData)
        })
    }
    // other endpoints like tickets, buses can go here later
};
