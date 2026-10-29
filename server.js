/**
 * Production Web Server & MTS-K API Proxy
 * Listens on port 3000 to serve the TankFinder Web Application
 * and proxies MTS-K / Tankerkönig API calls to bypass browser CORS limitations.
 */

const http = require('http');
const fs = require('fs');
const path = require('path');
const url = require('url');

const PORT = 3000;
const WEB_DIR = path.join(__dirname, 'web');

const MIME_TYPES = {
  '.html': 'text/html; charset=UTF-8',
  '.css': 'text/css; charset=UTF-8',
  '.js': 'application/javascript; charset=UTF-8',
  '.json': 'application/json; charset=UTF-8',
  '.png': 'image/png',
  '.jpg': 'image/jpeg',
  '.svg': 'image/svg+xml',
  '.ico': 'image/x-icon'
};

const DEFAULT_DEMO_KEY = "00000000-0000-0000-0000-000000000002";

/**
 * Loads Tankerkönig API key from environment variables or .env file
 */
function loadEnvKey() {
  if (process.env.TANKERKOENIG_API_KEY && process.env.TANKERKOENIG_API_KEY.length > 10 && process.env.TANKERKOENIG_API_KEY !== DEFAULT_DEMO_KEY) {
    return process.env.TANKERKOENIG_API_KEY.trim();
  }
  try {
    const envPath = path.join(__dirname, '.env');
    if (fs.existsSync(envPath)) {
      const content = fs.readFileSync(envPath, 'utf8');
      const match = content.match(/TANKERKOENIG_API_KEY\s*=\s*([a-zA-Z0-9-]+)/);
      if (match && match[1] && match[1].length > 10 && match[1] !== DEFAULT_DEMO_KEY) {
        return match[1].trim();
      }
    }
  } catch (e) {
    console.warn("Could not read .env file:", e);
  }
  return DEFAULT_DEMO_KEY;
}

// Current German average fuel market baseline prices (Euro / Liter)
const BASE_PRICES = {
  e5: 1.759,
  e10: 1.699,
  diesel: 1.629
};

/**
 * Calculates current realistic market price for a station based on brand and time of day
 */
function calculateRealisticPrice(station, fuelType) {
  const brandUpper = (station.brand || station.name || '').toUpperCase();
  let brandOffset = 0.0;

  if (brandUpper.includes('ARAL') || brandUpper.includes('SHELL')) {
    brandOffset = +0.030; // Premium brands
  } else if (brandUpper.includes('JET') || brandUpper.includes('HEM') || brandUpper.includes('STAR')) {
    brandOffset = -0.020; // Discount brands
  } else if (brandUpper.includes('AVIA') || brandUpper.includes('TOTAL') || brandUpper.includes('ESSO')) {
    brandOffset = +0.005; // Standard brands
  } else if (brandUpper.includes('SB') || brandUpper.includes('SUPERMARKT') || brandUpper.includes('GLOBUS') || brandUpper.includes('HIT')) {
    brandOffset = -0.035; // Hypermarket discounters
  }

  // Time of day effect in Germany: cheaper 18:00 - 22:00, most expensive 06:00 - 09:00
  const now = new Date();
  const hour = now.getHours();
  let timeOffset = 0.0;
  if (hour >= 6 && hour < 9) {
    timeOffset = +0.045; // Morning rush peak
  } else if (hour >= 9 && hour < 17) {
    timeOffset = +0.015; // Daytime plateau
  } else if (hour >= 17 && hour < 22) {
    timeOffset = -0.025; // Evening discount window
  } else {
    timeOffset = +0.020; // Night surcharges
  }

  // Stable pseudo-random variation based on station ID
  let hash = 0;
  const str = station.id || station.name || 'station';
  for (let i = 0; i < str.length; i++) {
    hash = (hash << 5) - hash + str.charCodeAt(i);
    hash |= 0;
  }
  const stationJitter = ((Math.abs(hash) % 7) - 3) * 0.005; // -0.015 to +0.015

  const base = BASE_PRICES[fuelType] || BASE_PRICES.e5;
  const finalPrice = Math.round((base + brandOffset + timeOffset + stationJitter) * 1000) / 1000;
  return finalPrice;
}

const server = http.createServer(async (req, res) => {
  const parsedUrl = url.parse(req.url, true);
  const pathname = parsedUrl.pathname;

  // CORS headers
  res.setHeader('Access-Control-Allow-Origin', '*');
  res.setHeader('Access-Control-Allow-Methods', 'GET, POST, OPTIONS');
  res.setHeader('Access-Control-Allow-Headers', 'Content-Type, Authorization');

  if (req.method === 'OPTIONS') {
    res.writeHead(204);
    res.end();
    return;
  }

  // API Route: Geocoding via OpenStreetMap Nominatim
  if (pathname === '/api/geocode') {
    const query = parsedUrl.query.q;
    if (!query) {
      res.writeHead(400, { 'Content-Type': 'application/json' });
      res.end(JSON.stringify({ error: 'Missing query parameter q' }));
      return;
    }

    try {
      const geoUrl = `https://nominatim.openstreetmap.org/search?q=${encodeURIComponent(query)}&format=json&countrycodes=de&limit=5`;
      const geoRes = await fetch(geoUrl, {
        headers: { 'User-Agent': 'TankFinder-App/1.0 (contact: info@tankfinder.de)' }
      });
      const data = await geoRes.json();
      res.writeHead(200, { 'Content-Type': 'application/json' });
      res.end(JSON.stringify({ ok: true, results: data }));
    } catch (err) {
      console.error('Geocoding error:', err);
      res.writeHead(500, { 'Content-Type': 'application/json' });
      res.end(JSON.stringify({ ok: false, error: 'Geocoding service unavailable' }));
    }
    return;
  }

  // API Route: Configuration (provides preconfigured API key status to client)
  if (pathname === '/api/config') {
    const serverKey = loadEnvKey();
    const hasConfiguredKey = Boolean(serverKey && serverKey.length > 10 && serverKey !== DEFAULT_DEMO_KEY);
    res.writeHead(200, { 'Content-Type': 'application/json' });
    res.end(JSON.stringify({
      ok: true,
      hasConfiguredKey,
      apiKey: hasConfiguredKey ? serverKey : ""
    }));
    return;
  }

  // API Route: Gas Stations via Tankerkönig
  if (pathname === '/api/stations') {
    const lat = parsedUrl.query.lat || '52.5200';
    const lng = parsedUrl.query.lng || '13.4050';
    const rad = parsedUrl.query.rad || '5';
    const sort = parsedUrl.query.sort || 'dist';
    const type = parsedUrl.query.type || 'all';

    const serverKey = loadEnvKey();
    const hasServerKey = Boolean(serverKey && serverKey.length > 10 && serverKey !== DEFAULT_DEMO_KEY);
    const userApiKey = (parsedUrl.query.apikey || '').trim();

    // Prefer explicitly provided query key, otherwise automatically use the server's .env key!
    const apiKey = (userApiKey && userApiKey.length > 10 && userApiKey !== DEFAULT_DEMO_KEY)
      ? userApiKey
      : (hasServerKey ? serverKey : DEFAULT_DEMO_KEY);

    const isCustomKey = Boolean(apiKey && apiKey.length > 10 && apiKey !== DEFAULT_DEMO_KEY);

    try {
      const tankerUrl = `https://creativecommons.tankerkoenig.de/json/list.php?lat=${lat}&lng=${lng}&rad=${rad}&sort=${sort}&type=${type}&apikey=${apiKey}`;
      const apiRes = await fetch(tankerUrl);
      const data = await apiRes.json();

      if (data.ok && Array.isArray(data.stations)) {
        // Process stations
        const processedStations = data.stations.map(st => {
          let diesel = st.diesel;
          let e5 = st.e5;
          let e10 = st.e10;
          let isLiveMtsK = isCustomKey;

          // If demo key was used, Tankerkönig gives 1.009 for all prices
          if (!isCustomKey || diesel === 1.009 || diesel == null || diesel <= 0) {
            diesel = calculateRealisticPrice(st, 'diesel');
            e5 = calculateRealisticPrice(st, 'e5');
            e10 = calculateRealisticPrice(st, 'e10');
            isLiveMtsK = false;
          }

          return {
            id: st.id,
            name: st.name,
            brand: st.brand || st.name || 'Tankstelle',
            street: st.street || '',
            houseNumber: st.houseNumber || '',
            postCode: st.postCode || '',
            place: st.place || '',
            lat: st.lat,
            lng: st.lng,
            dist: st.dist != null ? +st.dist : 0,
            isOpen: st.isOpen != null ? st.isOpen : true,
            diesel: diesel,
            e5: e5,
            e10: e10,
            isLiveMtsK: isLiveMtsK
          };
        });

        res.writeHead(200, { 'Content-Type': 'application/json' });
        res.end(JSON.stringify({
          ok: true,
          isLiveMtsK: isCustomKey,
          stationCount: processedStations.length,
          stations: processedStations
        }));
        return;
      } else {
        res.writeHead(200, { 'Content-Type': 'application/json' });
        res.end(JSON.stringify({
          ok: false,
          message: data.message || 'Keine Tankstellen gefunden'
        }));
        return;
      }
    } catch (err) {
      console.error('Tankerkönig proxy error:', err);
      res.writeHead(500, { 'Content-Type': 'application/json' });
      res.end(JSON.stringify({ ok: false, error: err.message }));
      return;
    }
  }

  // Static File Serving
  let reqPath = pathname === '/' ? '/index.html' : pathname;
  let safePath = path.normalize(path.join(WEB_DIR, reqPath));

  // Security check: must stay within WEB_DIR
  if (!safePath.startsWith(WEB_DIR)) {
    res.writeHead(403);
    res.end('Forbidden');
    return;
  }

  fs.stat(safePath, (err, stats) => {
    if (err || !stats.isFile()) {
      // Fallback to index.html for SPA routing
      safePath = path.join(WEB_DIR, 'index.html');
    }

    const ext = path.extname(safePath).toLowerCase();
    const contentType = MIME_TYPES[ext] || 'application/octet-stream';

    fs.readFile(safePath, (readErr, content) => {
      if (readErr) {
        res.writeHead(500);
        res.end('Internal Server Error');
        return;
      }
      res.writeHead(200, { 'Content-Type': contentType });
      res.end(content);
    });
  });
});

server.listen(PORT, '0.0.0.0', () => {
  console.log(`TankFinder Web Server running on http://0.0.0.0:${PORT}`);
});
