// State & Chart instances
let timelineChartInstance = null;
let deviceChartInstance = null;
let browserChartInstance = null;

document.addEventListener('DOMContentLoaded', () => {
    initApp();
});

function initApp() {
    loadOverviewStats();
    loadRecentUrls();
    setupEventListeners();
}

function setupEventListeners() {
    // Form Submission
    const shortenForm = document.getElementById('shortenForm');
    shortenForm.addEventListener('submit', handleShortenSubmit);

    // Advanced Options Toggle
    const advancedToggle = document.getElementById('advancedToggle');
    const advancedPanel = document.getElementById('advancedPanel');
    advancedToggle.addEventListener('click', () => {
        advancedToggle.classList.toggle('open');
        advancedPanel.classList.toggle('show');
    });

    // Copy Button
    const copyBtn = document.getElementById('copyBtn');
    copyBtn.addEventListener('click', () => {
        const url = document.getElementById('shortUrlDisplay').textContent;
        if (url) {
            navigator.clipboard.writeText(url).then(() => {
                showToast('Short URL copied to clipboard!');
            });
        }
    });

    // Refresh Button
    document.getElementById('refreshBtn').addEventListener('click', () => {
        loadOverviewStats();
        loadRecentUrls();
        showToast('Data refreshed');
    });

    // Modal Close
    document.getElementById('closeModalBtn').addEventListener('click', closeModal);
    document.getElementById('modalOverlay').addEventListener('click', closeModal);
}

// Handle URL Shortening
async function handleShortenSubmit(e) {
    e.preventDefault();
    const originalUrl = document.getElementById('originalUrl').value.trim();
    const customAlias = document.getElementById('customAlias').value.trim();
    const expirationDaysVal = document.getElementById('expirationDays').value.trim();
    const expirationDays = expirationDaysVal ? parseInt(expirationDaysVal, 10) : null;

    const shortenBtn = document.getElementById('shortenBtn');
    const originalBtnHtml = shortenBtn.innerHTML;
    shortenBtn.disabled = true;
    shortenBtn.innerHTML = '<i class="fa-solid fa-spinner fa-spin"></i> Shortening...';

    try {
        const payload = {
            originalUrl,
            customAlias: customAlias || null,
            expirationDays: expirationDays
        };

        const response = await fetch('/api/v1/urls/shorten', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });

        const data = await response.json();

        if (response.ok && data.success) {
            displayResult(data.data);
            showToast('URL shortened successfully!');
            document.getElementById('originalUrl').value = '';
            document.getElementById('customAlias').value = '';
            document.getElementById('expirationDays').value = '';
            loadOverviewStats();
            loadRecentUrls();
        } else {
            showToast(data.message || 'Failed to shorten URL');
        }
    } catch (err) {
        showToast('Error connecting to server: ' + err.message);
    } finally {
        shortenBtn.disabled = false;
        shortenBtn.innerHTML = originalBtnHtml;
    }
}

// Display Shorten Result
function displayResult(result) {
    const resultBox = document.getElementById('resultBox');
    const shortUrlDisplay = document.getElementById('shortUrlDisplay');
    const originalUrlDisplay = document.getElementById('originalUrlDisplay');
    const qrThumbnail = document.getElementById('qrThumbnail');
    const downloadQrBtn = document.getElementById('downloadQrBtn');
    const resultExpires = document.getElementById('resultExpires');

    shortUrlDisplay.href = result.shortUrl;
    shortUrlDisplay.textContent = result.shortUrl;
    originalUrlDisplay.textContent = result.originalUrl;
    qrThumbnail.src = result.qrCodeUrl;
    downloadQrBtn.href = result.qrCodeUrl;

    if (result.expiresAt) {
        const d = new Date(result.expiresAt);
        resultExpires.textContent = `Expires: ${d.toLocaleDateString()}`;
    } else {
        resultExpires.textContent = 'Never expires';
    }

    resultBox.classList.remove('hidden');
    resultBox.scrollIntoView({ behavior: 'smooth', block: 'nearest' });
}

// Load Stats
async function loadOverviewStats() {
    try {
        const res = await fetch('/api/v1/urls/stats/overview');
        if (res.ok) {
            const data = await res.json();
            if (data.success) {
                document.getElementById('statTotalUrls').textContent = data.data.totalUrls.toLocaleString();
                document.getElementById('statTotalClicks').textContent = data.data.totalClicks.toLocaleString();
                document.getElementById('statActiveUrls').textContent = data.data.activeUrls.toLocaleString();
            }
        }
    } catch (e) {
        console.error('Failed to load stats', e);
    }
}

// Load Recent URLs
async function loadRecentUrls() {
    const tbody = document.getElementById('urlTableBody');
    try {
        const res = await fetch('/api/v1/urls');
        if (!res.ok) throw new Error('Network response not ok');
        const data = await res.json();

        if (data.success && data.data && data.data.length > 0) {
            tbody.innerHTML = '';
            data.data.forEach(item => {
                const tr = document.createElement('tr');
                const isExpired = item.expiresAt && new Date(item.expiresAt) < new Date();
                const statusBadge = isExpired
                    ? '<span class="badge-expired"><i class="fa-solid fa-clock"></i> Expired</span>'
                    : '<span class="badge-active"><i class="fa-solid fa-check"></i> Active</span>';

                tr.innerHTML = `
                    <td>
                        <a href="${item.shortUrl}" target="_blank" class="short-code-badge">${item.shortCode}</a>
                    </td>
                    <td>
                        <span class="truncate-dest" title="${item.originalUrl}">${escapeHtml(item.originalUrl)}</span>
                    </td>
                    <td>
                        <span class="click-badge"><i class="fa-solid fa-mouse-pointer"></i> ${item.clickCount}</span>
                    </td>
                    <td>${formatDate(item.createdAt)}</td>
                    <td>${statusBadge}</td>
                    <td>
                        <div class="action-buttons">
                            <button class="btn-icon" title="Copy short link" onclick="copyShortLink('${item.shortUrl}')">
                                <i class="fa-regular fa-copy"></i>
                            </button>
                            <button class="btn-icon" title="View Analytics" onclick="openAnalytics('${item.shortCode}')">
                                <i class="fa-solid fa-chart-simple"></i>
                            </button>
                            <button class="btn-icon" title="Delete URL" onclick="deleteUrl('${item.shortCode}')">
                                <i class="fa-regular fa-trash-can"></i>
                            </button>
                        </div>
                    </td>
                `;
                tbody.appendChild(tr);
            });
        } else {
            tbody.innerHTML = '<tr><td colspan="6" class="text-center" style="padding: 24px;">No URLs shortened yet. Create your first link above!</td></tr>';
        }
    } catch (e) {
        tbody.innerHTML = '<tr><td colspan="6" class="text-center" style="color: #ef4444; padding: 20px;">Failed to load URLs. Please try refreshing.</td></tr>';
    }
}

// Copy Helper
function copyShortLink(url) {
    navigator.clipboard.writeText(url).then(() => {
        showToast('Short URL copied!');
    });
}

// Delete URL
async function deleteUrl(shortCode) {
    if (!confirm(`Are you sure you want to delete short URL "${shortCode}"? All click analytics will be removed.`)) {
        return;
    }

    try {
        const res = await fetch(`/api/v1/urls/${shortCode}`, { method: 'DELETE' });
        const data = await res.json();
        if (res.ok && data.success) {
            showToast('URL deleted successfully');
            loadOverviewStats();
            loadRecentUrls();
        } else {
            showToast(data.message || 'Failed to delete URL');
        }
    } catch (e) {
        showToast('Error deleting URL: ' + e.message);
    }
}

// Analytics Modal
async function openAnalytics(shortCode) {
    const modal = document.getElementById('analyticsModal');
    modal.classList.remove('hidden');

    document.getElementById('modalTitle').textContent = `Analytics: ${shortCode}`;
    document.getElementById('modalShortCode').textContent = shortCode;

    try {
        const res = await fetch(`/api/v1/urls/${shortCode}/analytics`);
        const data = await res.json();

        if (res.ok && data.success) {
            const a = data.data;
            document.getElementById('modalTotalClicks').textContent = a.totalClicks.toLocaleString();
            const destLink = document.getElementById('modalDestinationLink');
            destLink.href = a.originalUrl;
            destLink.textContent = a.originalUrl;
            destLink.title = a.originalUrl;

            renderTimelineChart(a.clicksByDate);
            renderDeviceChart(a.deviceBreakdown);
            renderBrowserChart(a.browserBreakdown);
            renderReferrers(a.refererBreakdown);
            renderRecentClicks(a.recentClicks);
        } else {
            showToast('Failed to load analytics: ' + data.message);
        }
    } catch (e) {
        showToast('Error fetching analytics: ' + e.message);
    }
}

function closeModal() {
    document.getElementById('analyticsModal').classList.add('hidden');
    if (timelineChartInstance) timelineChartInstance.destroy();
    if (deviceChartInstance) deviceChartInstance.destroy();
    if (browserChartInstance) browserChartInstance.destroy();
}

// Chart Renderers
function renderTimelineChart(clicksByDate) {
    const ctx = document.getElementById('timelineChart').getContext('2d');
    if (timelineChartInstance) timelineChartInstance.destroy();

    const labels = Object.keys(clicksByDate || {});
    const values = Object.values(clicksByDate || {});

    timelineChartInstance = new Chart(ctx, {
        type: 'line',
        data: {
            labels: labels.length > 0 ? labels : ['No data'],
            datasets: [{
                label: 'Clicks',
                data: values.length > 0 ? values : [0],
                borderColor: '#8b5cf6',
                backgroundColor: 'rgba(139, 92, 246, 0.2)',
                fill: true,
                tension: 0.35,
                borderWidth: 2,
                pointBackgroundColor: '#38bdf8'
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: { legend: { display: false } },
            scales: {
                y: { beginAtZero: true, grid: { color: 'rgba(255,255,255,0.05)' } },
                x: { grid: { color: 'rgba(255,255,255,0.05)' } }
            }
        }
    });
}

function renderDeviceChart(devices) {
    const ctx = document.getElementById('deviceChart').getContext('2d');
    if (deviceChartInstance) deviceChartInstance.destroy();

    const labels = Object.keys(devices || {});
    const values = Object.values(devices || {});

    deviceChartInstance = new Chart(ctx, {
        type: 'doughnut',
        data: {
            labels: labels.length > 0 ? labels : ['Desktop'],
            datasets: [{
                data: values.length > 0 ? values : [0],
                backgroundColor: ['#8b5cf6', '#38bdf8', '#10b981', '#f59e0b', '#ec4899'],
                borderWidth: 0
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: { position: 'bottom', labels: { color: '#94a3b8', font: { size: 11 } } }
            }
        }
    });
}

function renderBrowserChart(browsers) {
    const ctx = document.getElementById('browserChart').getContext('2d');
    if (browserChartInstance) browserChartInstance.destroy();

    const labels = Object.keys(browsers || {});
    const values = Object.values(browsers || {});

    browserChartInstance = new Chart(ctx, {
        type: 'bar',
        data: {
            labels: labels.length > 0 ? labels : ['Direct'],
            datasets: [{
                label: 'Clicks',
                data: values.length > 0 ? values : [0],
                backgroundColor: '#38bdf8',
                borderRadius: 4
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: { legend: { display: false } },
            scales: {
                y: { beginAtZero: true, grid: { color: 'rgba(255,255,255,0.05)' } },
                x: { grid: { color: 'rgba(255,255,255,0.05)' } }
            }
        }
    });
}

function renderReferrers(referrers) {
    const list = document.getElementById('refererList');
    list.innerHTML = '';
    const entries = Object.entries(referrers || {});
    if (entries.length === 0) {
        list.innerHTML = '<li style="color: #64748b;">No referrer data recorded yet.</li>';
        return;
    }
    entries.forEach(([ref, count]) => {
        const li = document.createElement('li');
        li.innerHTML = `<span>${escapeHtml(ref)}</span><span>${count}</span>`;
        list.appendChild(li);
    });
}

function renderRecentClicks(clicks) {
    const tbody = document.getElementById('modalClicksBody');
    tbody.innerHTML = '';
    if (!clicks || clicks.length === 0) {
        tbody.innerHTML = '<tr><td colspan="6" class="text-center" style="padding: 16px;">No visits recorded yet.</td></tr>';
        return;
    }
    clicks.forEach(c => {
        const tr = document.createElement('tr');
        tr.innerHTML = `
            <td>${formatDate(c.timestamp)}</td>
            <td><code>${escapeHtml(c.ipAddress || '127.0.0.1')}</code></td>
            <td>${escapeHtml(c.browser || 'Unknown')}</td>
            <td>${escapeHtml(c.os || 'Unknown')}</td>
            <td>${escapeHtml(c.deviceType || 'Desktop')}</td>
            <td>${escapeHtml(c.referer || 'Direct')}</td>
        `;
        tbody.appendChild(tr);
    });
}

// Helpers
function formatDate(dateStr) {
    if (!dateStr) return '---';
    const d = new Date(dateStr);
    return d.toLocaleDateString() + ' ' + d.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
}

function escapeHtml(text) {
    if (!text) return '';
    return text.replace(/[&<>"']/g, m => ({
        '&': '&amp;',
        '<': '&lt;',
        '>': '&gt;',
        '"': '&quot;',
        "'": '&#39;'
    })[m]);
}

function showToast(message) {
    const toast = document.getElementById('toast');
    toast.textContent = message;
    toast.classList.remove('hidden');
    setTimeout(() => {
        toast.classList.add('hidden');
    }, 3200);
}

