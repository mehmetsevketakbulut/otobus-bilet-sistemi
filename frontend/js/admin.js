/**
 * Admin Panel JavaScript
 * Tüm veriler gerçek API'den çekilir (demo veri kullanılmaz).
 */

// ── Protected API helper with Bearer token ──
function adminFetch(endpoint, options = {}) {
    const token = localStorage.getItem('jwt_token');
    const headers = { 'Content-Type': 'application/json', ...(options.headers || {}) };
    if (token) headers['Authorization'] = `Bearer ${token}`;
    return fetch(`${typeof API_BASE_URL !== 'undefined' ? API_BASE_URL : '/api'}${endpoint}`, { ...options, headers })
        .then(r => {
            if (r.status === 401 || r.status === 403) {
                localStorage.removeItem('jwt_token');
                location.href = 'login.html';
                throw new Error('Yetki hatası');
            }
            if (!r.ok) throw new Error(r.statusText);
            return r.json();
        });
}

document.addEventListener('DOMContentLoaded', () => {
    // Auth kontrolü — Token yoksa veya ADMIN değilse yönlendir
    const token = localStorage.getItem('jwt_token');
    if (!token) { location.href = 'login.html'; return; }
    try {
        const payload = JSON.parse(atob(token.split('.')[1]));
        if (payload.role !== 'ADMIN') { location.href = 'index.html'; return; }
    } catch (e) { location.href = 'login.html'; return; }

    // ── Dashboard Verileri (Gerçek API'den) ──
    loadDashboard();
    loadCompanies();
    loadPendingTrips();
    loadUsers();
    loadTerminals();
    loadAuditLogs();
    loadBuses();

    // ── Sidebar Nav ──
    const pages = { dashboard:'Gösterge Paneli', companies:'Firmalar', pendingTrips:'Sefer Talepleri', users:'Kullanıcılar', terminals:'Terminaller', buses:'Otobüsler', logs:'İşlem Logları', settings:'Sistem Ayarları' };
    document.querySelectorAll('.sb-link').forEach(btn => {
        btn.addEventListener('click', () => {
            const pg = btn.dataset.page;
            document.querySelectorAll('.sb-link').forEach(b => b.classList.remove('active'));
            btn.classList.add('active');
            document.getElementById('pageTitle').textContent = pages[pg] || pg;
            Object.keys(pages).forEach(p => {
                const el = document.getElementById('pg' + p.charAt(0).toUpperCase() + p.slice(1));
                if (el) el.classList.toggle('hidden', p !== pg);
            });
            document.getElementById('sidebar').classList.add('-translate-x-full');
            document.getElementById('sbOverlay').classList.add('hidden');
        });
    });

    // Terminal ekleme formu
    document.getElementById('termForm').addEventListener('submit', e => {
        e.preventDefault();
        const cityName = document.getElementById('termCity').value.trim();
        const termName = document.getElementById('termName').value.trim();

        // Önce şehri ekle (veya var olanı al), sonra terminali oluştur
        adminFetch('/cities', {
            method: 'POST',
            body: JSON.stringify({ name: cityName })
        }).catch(() => {
            // Şehir zaten varsa devam et
            return adminFetch('/cities').then(cities => cities.find(c => c.name === cityName));
        }).then(cityData => {
            // Şehri bulduktan sonra terminali ekle
            return adminFetch('/cities').then(cities => {
                const city = cities.find(c => c.name === cityName);
                if (!city) throw new Error('Şehir bulunamadı');
                return adminFetch('/terminals', {
                    method: 'POST',
                    body: JSON.stringify({ name: termName, city: { id: city.id } })
                });
            });
        }).then(() => {
            loadTerminals();
            e.target.reset();
        }).catch(err => alert('Terminal eklenirken hata: ' + err.message));
    });
});

// ── Dashboard Yükleme ──
function loadDashboard() {
    adminFetch('/admin/stats').then(stats => {
        const cards = document.querySelectorAll('.stat-card .text-2xl');
        if (cards[0]) cards[0].textContent = stats.totalCompanies || 0;
        if (cards[1]) cards[1].textContent = (stats.totalUsers || 0).toLocaleString('tr-TR');
        if (cards[2]) cards[2].textContent = (stats.totalTrips || 0).toLocaleString('tr-TR');
        if (cards[3]) cards[3].textContent = (stats.totalTickets || 0).toLocaleString('tr-TR');
    }).catch(err => console.error('Dashboard yüklenemedi:', err));
}

// ── Firmalar ──
let companiesData = [];
function loadCompanies() {
    adminFetch('/admin/companies').then(companies => {
        companiesData = companies;
        renderCompanies(companies);
    }).catch(err => console.error('Firmalar yüklenemedi:', err));
}

function renderCompanies(data) {
    const tb = document.getElementById('compTable');
    if (!data.length) {
        tb.innerHTML = '<tr><td colspan="6" class="px-6 py-8 text-center text-gray-600">Kayıtlı firma bulunamadı.</td></tr>';
        return;
    }
    tb.innerHTML = data.map(c => {
        const isActive = c.active !== false;
        const statusBg = isActive ? 'bg-emerald-500/15 text-emerald-400' : 'bg-red-500/15 text-red-400';
        const statusText = isActive ? 'Aktif' : 'Pasif';
        return `<tr class="tbl-row border-b border-white/5">
            <td class="px-6 py-3.5"><div class="flex items-center gap-3">
                <div class="w-9 h-9 rounded-lg bg-indigo-500/10 flex items-center justify-center text-indigo-400 font-bold text-xs">${(c.name || '').substring(0,2).toUpperCase()}</div>
                <span class="font-semibold text-white">${c.name || '-'}</span>
            </div></td>
            <td class="px-6 py-3.5 font-mono text-xs text-gray-500">${c.id}</td>
            <td class="px-6 py-3.5 text-gray-400">${c.owner ? c.owner.fullName : '-'}</td>
            <td class="px-6 py-3.5 text-gray-500 text-xs">-</td>
            <td class="px-6 py-3.5">
                <button onclick="toggleCompanyActive(${c.id})" class="px-2 py-0.5 text-[10px] font-bold rounded-md ${statusBg} cursor-pointer hover:opacity-80 transition">${statusText}</button>
            </td>
            <td class="px-6 py-3.5 text-right">
                <button onclick="deleteCompany(${c.id})" class="px-3 py-1.5 text-xs font-bold rounded-lg bg-red-500/10 text-red-400 hover:bg-red-500/20 transition cursor-pointer">Sil</button>
            </td>
        </tr>`;
    }).join('');
}

window.toggleCompanyActive = (id) => {
    adminFetch(`/admin/companies/${id}/toggle-active`, { method: 'PUT' })
        .then(() => loadCompanies())
        .catch(err => alert('Durum değiştirme hatası: ' + err.message));
};

window.deleteCompany = (id) => {
    if (!confirm('Bu firmayı silmek istediğinize emin misiniz?')) return;
    adminFetch(`/admin/companies/${id}`, { method: 'DELETE' })
        .then(() => loadCompanies())
        .catch(err => alert('Silme hatası: ' + err.message));
};

// Search
const cs = document.getElementById('compSearch');
if (cs) cs.addEventListener('input', e => {
    renderCompanies(companiesData.filter(c => (c.name || '').toLowerCase().includes(e.target.value.toLowerCase())));
});

// ── Kullanıcılar ──
function loadUsers() {
    adminFetch('/admin/users').then(users => {
        const tb = document.getElementById('userTable');
        if (!users.length) {
            tb.innerHTML = '<tr><td colspan="5" class="px-6 py-8 text-center text-gray-600">Kullanıcı bulunamadı.</td></tr>';
            return;
        }
        tb.innerHTML = users.map(u => {
            const role = u.role || 'USER';
            const roleBg = role==='ADMIN' ? 'bg-indigo-500/15 text-indigo-400' : role==='COMPANY' ? 'bg-amber-500/15 text-amber-400' : 'bg-emerald-500/15 text-emerald-400';
            const verified = u.emailVerified ? '✅' : '❌';
            return `<tr class="tbl-row border-b border-white/5">
                <td class="px-6 py-3.5 font-semibold text-white">${u.fullName || '-'}</td>
                <td class="px-6 py-3.5 text-gray-500 text-xs">${u.email || '-'}</td>
                <td class="px-6 py-3.5"><span class="px-2 py-0.5 text-[10px] font-bold rounded-md ${roleBg}">${role}</span></td>
                <td class="px-6 py-3.5 text-gray-500 text-xs">${verified}</td>
                <td class="px-6 py-3.5">
                    ${role !== 'ADMIN' ? `<button onclick="deleteUser(${u.id})" class="px-2 py-1 text-xs rounded-lg bg-red-500/10 text-red-400 hover:bg-red-500/20 transition cursor-pointer">Sil</button>` : ''}
                </td>
            </tr>`;
        }).join('');
    }).catch(err => console.error('Kullanıcılar yüklenemedi:', err));
}

window.deleteUser = (id) => {
    if (!confirm('Bu kullanıcıyı silmek istediğinize emin misiniz?')) return;
    adminFetch(`/users/${id}`, { method: 'DELETE' })
        .then(() => loadUsers())
        .catch(err => alert('Silme hatası: ' + err.message));
};

// ── Terminaller ──
function loadTerminals() {
    adminFetch('/terminals').then(terminals => {
        const tb = document.getElementById('termTable');
        if (!terminals.length) {
            tb.innerHTML = '<tr><td colspan="3" class="px-6 py-8 text-center text-gray-600">Terminal bulunamadı.</td></tr>';
            return;
        }
        tb.innerHTML = terminals.map(t => `
            <tr class="tbl-row border-b border-white/5">
                <td class="px-6 py-3 font-semibold text-white">${t.city ? t.city.name : '-'}</td>
                <td class="px-6 py-3 text-gray-400">${t.name}</td>
                <td class="px-6 py-3 text-right">
                    <button onclick="removeTerminal(${t.id})" class="p-1.5 rounded-lg hover:bg-red-500/10 text-gray-600 hover:text-red-400 transition cursor-pointer">
                        <svg class="w-4 h-4" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><path d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16"/></svg>
                    </button>
                </td>
            </tr>`).join('');
    }).catch(err => console.error('Terminaller yüklenemedi:', err));
}

window.removeTerminal = (id) => {
    if (!confirm('Bu terminali silmek istediğinize emin misiniz?')) return;
    adminFetch(`/terminals/${id}`, { method: 'DELETE' })
        .then(() => loadTerminals())
        .catch(err => alert('Silme hatası: ' + err.message));
};

// ── Audit Logları ──
function loadAuditLogs() {
    adminFetch('/admin/audit-logs').then(logs => {
        const tb = document.getElementById('logTable');
        if (!tb) return;
        if (!logs.length) {
            tb.innerHTML = '<tr><td colspan="5" class="px-6 py-8 text-center text-gray-600">İşlem logu bulunamadı.</td></tr>';
            return;
        }
        tb.innerHTML = logs.map(l => {
            const actionColors = {
                'CREATE': 'bg-emerald-500/15 text-emerald-400',
                'UPDATE': 'bg-amber-500/15 text-amber-400',
                'DELETE': 'bg-red-500/15 text-red-400',
                'LOGIN': 'bg-indigo-500/15 text-indigo-400',
                'LOGIN_FAILED': 'bg-red-500/15 text-red-400',
                'PASSWORD_RESET': 'bg-purple-500/15 text-purple-400'
            };
            const color = actionColors[l.action] || 'bg-gray-500/15 text-gray-400';
            const date = l.timestamp ? new Date(l.timestamp).toLocaleString('tr-TR') : '-';
            return `<tr class="tbl-row border-b border-white/5">
                <td class="px-6 py-3 text-gray-500 text-xs">${date}</td>
                <td class="px-6 py-3 text-white text-xs">${l.user ? l.user.fullName : 'Sistem'}</td>
                <td class="px-6 py-3"><span class="px-2 py-0.5 text-[10px] font-bold rounded-md ${color}">${l.action}</span></td>
                <td class="px-6 py-3 text-gray-400 text-xs">${l.entityType || '-'}</td>
                <td class="px-6 py-3 text-gray-500 text-xs">${l.details || '-'}</td>
            </tr>`;
        }).join('');
    }).catch(err => console.error('Loglar yüklenemedi:', err));
}

function toggleSB() {
    document.getElementById('sidebar').classList.toggle('-translate-x-full');
    document.getElementById('sbOverlay').classList.toggle('hidden');
}

// ── Sefer Talepleri (Pending Trips) ──
function loadPendingTrips() {
    adminFetch('/trips/admin/pending').then(trips => {
        const tb = document.getElementById('pendingTable');
        const badge = document.getElementById('pendingBadge');

        // Badge güncelle
        if (badge) {
            if (trips.length > 0) {
                badge.textContent = trips.length;
                badge.classList.remove('hidden');
            } else {
                badge.classList.add('hidden');
            }
        }

        if (!tb) return;
        if (!trips.length) {
            tb.innerHTML = '<tr><td colspan="6" class="px-6 py-8 text-center text-gray-600">Onay bekleyen sefer bulunmuyor. ✅</td></tr>';
            return;
        }
        tb.innerHTML = trips.map(t => {
            const fromCity = t.kalkisTerminali?.city?.name || t.kalkisTerminali?.name || '-';
            const toCity = t.varisTerminali?.city?.name || t.varisTerminali?.name || '-';
            const busPlate = t.otobus?.plate || '-';
            const seatCap = t.otobus?.seatCapacity || '-';
            const companyName = t.otobus?.company?.name || '-';
            const price = t.fiyat || 0;
            const dateStr = t.kalkisSaati ? new Date(t.kalkisSaati).toLocaleString('tr-TR', { day:'numeric', month:'short', year:'numeric', hour:'2-digit', minute:'2-digit' }) : '-';
            const stopCount = t.stops ? t.stops.length : 0;

            // Durak detayları
            let stopsHtml = '';
            if (t.stops && t.stops.length > 0) {
                const sortedStops = [...t.stops].sort((a, b) => a.stopOrder - b.stopOrder);
                stopsHtml = sortedStops.map((s, i) => {
                    const sName = s.terminal?.city?.name || s.terminal?.name || '?';
                    const sTime = s.departureTime ? new Date(s.departureTime).toLocaleString('tr-TR', { day:'numeric', month:'short', hour:'2-digit', minute:'2-digit' }) : '';
                    const icon = i === 0 ? '🚀' : i === sortedStops.length - 1 ? '🏁' : '🚏';
                    return `<span class="text-[10px] bg-white/5 px-1.5 py-0.5 rounded">${icon} ${sName} <span class="text-gray-600">${sTime}</span></span>`;
                }).join(' → ');
            }

            return `<tr class="tbl-row border-b border-white/5">
                <td class="px-6 py-3.5">
                    <div class="flex flex-col gap-1">
                        <div class="flex items-center gap-2">
                            <span class="font-semibold text-white">${fromCity}</span>
                            <svg class="w-3 h-3 text-gray-600" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><path d="M14 5l7 7m0 0l-7 7m7-7H3"/></svg>
                            <span class="font-semibold text-white">${toCity}</span>
                        </div>
                        <span class="text-[10px] text-gray-600">${companyName}</span>
                    </div>
                </td>
                <td class="px-6 py-3.5">
                    <div class="flex flex-wrap items-center gap-1 max-w-xs">${stopsHtml || '<span class="text-gray-600 text-xs">Durak yok</span>'}</div>
                </td>
                <td class="px-6 py-3.5 text-gray-400 text-xs">${dateStr}</td>
                <td class="px-6 py-3.5">
                    <span class="text-xs bg-white/5 px-2 py-0.5 rounded font-medium text-gray-400">${busPlate} · ${seatCap} koltuk</span>
                </td>
                <td class="px-6 py-3.5 font-bold text-white">${price} ₺</td>
                <td class="px-6 py-3.5 text-right">
                    <div class="flex items-center justify-end gap-2">
                        <button onclick="approveTrip(${t.id})" class="px-3 py-1.5 text-xs font-bold rounded-lg bg-emerald-500/15 text-emerald-400 hover:bg-emerald-500/25 transition cursor-pointer">✅ Onayla</button>
                        <button onclick="rejectTrip(${t.id})" class="px-3 py-1.5 text-xs font-bold rounded-lg bg-red-500/15 text-red-400 hover:bg-red-500/25 transition cursor-pointer">❌ Reddet</button>
                    </div>
                </td>
            </tr>`;
        }).join('');
    }).catch(err => {
        console.error('Sefer talepleri yüklenemedi:', err);
        const tb = document.getElementById('pendingTable');
        if (tb) tb.innerHTML = '<tr><td colspan="6" class="px-6 py-8 text-center text-gray-600">Yüklenirken hata oluştu.</td></tr>';
    });
}

window.approveTrip = (id) => {
    if (!confirm('Bu seferi onaylamak istediğinize emin misiniz?')) return;
    adminFetch(`/trips/admin/approve/${id}`, { method: 'POST' })
        .then(() => {
            loadPendingTrips();
            alert('✅ Sefer onaylandı!');
        })
        .catch(err => alert('Onaylama hatası: ' + err.message));
};

window.rejectTrip = (id) => {
    if (!confirm('Bu seferi reddetmek istediğinize emin misiniz? Bu işlem geri alınamaz.')) return;
    adminFetch(`/trips/admin/reject/${id}`, { method: 'DELETE' })
        .then(() => {
            loadPendingTrips();
            alert('❌ Sefer reddedildi.');
        })
        .catch(err => alert('Reddetme hatası: ' + err.message));
};

// ── Otobüsler ──
function loadBuses() {
    adminFetch('/buses').then(buses => {
        const tb = document.getElementById('busTable');
        if (!tb) return;
        if (!buses.length) {
            tb.innerHTML = '<tr><td colspan="4" class="px-6 py-8 text-center text-gray-600">Kayıtlı otobüs bulunamadı.</td></tr>';
            return;
        }
        tb.innerHTML = buses.map(b => `
            <tr class="tbl-row border-b border-white/5">
                <td class="px-6 py-3 font-semibold text-white">${b.plate}</td>
                <td class="px-6 py-3 text-gray-400">${b.seatCapacity} koltuk</td>
                <td class="px-6 py-3 text-gray-400">${b.company ? b.company.name : '-'}</td>
                <td class="px-6 py-3 text-right">
                    <button onclick="removeBus(${b.id})" class="p-1.5 rounded-lg hover:bg-red-500/10 text-gray-600 hover:text-red-400 transition cursor-pointer">
                        <svg class="w-4 h-4" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><path d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16"/></svg>
                    </button>
                </td>
            </tr>`).join('');
    }).catch(err => console.error('Otobüsler yüklenemedi:', err));

    // Firma dropdown'u doldur
    adminFetch('/companies').then(companies => {
        const sel = document.getElementById('busCompany');
        if (!sel) return;
        const current = sel.innerHTML;
        if (current.includes('value="1"')) return;
        companies.forEach(c => {
            sel.innerHTML += `<option value="${c.id}">${c.name}</option>`;
        });
    }).catch(() => {});
}

window.removeBus = (id) => {
    if (!confirm('Bu otobüsü silmek istediğinize emin misiniz?')) return;
    adminFetch(`/buses/${id}`, { method: 'DELETE' })
        .then(() => loadBuses())
        .catch(err => alert('Silme hatası: ' + err.message));
};

// Otobüs ekleme formu
const busForm = document.getElementById('busForm');
if (busForm) {
    busForm.addEventListener('submit', e => {
        e.preventDefault();
        const plate = document.getElementById('busPlate').value.trim();
        const cap = parseInt(document.getElementById('busCap').value);
        const companyId = parseInt(document.getElementById('busCompany').value);

        adminFetch('/buses', {
            method: 'POST',
            body: JSON.stringify({ plate, seatCapacity: cap, company: { id: companyId } })
        }).then(() => {
            loadBuses();
            e.target.reset();
        }).catch(err => alert('Otobüs eklenirken hata: ' + err.message));
    });
}
