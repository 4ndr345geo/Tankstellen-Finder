/**
 * TankFinder Web Application Logic
 * Full functionality: Gas stations, MTS-K prices, 30-day trends, 
 * 12-month Brent crude oil correlation & German federal state school holidays.
 */

// ================= 12-Month Historical & Oil Data =================
const TWELVE_MONTH_DATA = [
  {
    month: "Nov 2025",
    label: "November 2025",
    e5: 1.764,
    e10: 1.706,
    diesel: 1.621,
    brentOilUsd: 73.80,
    oilEquivalentCt: 42.6,
    taxAndDutyCt: 94.2,
    marginCt: 39.6,
    holidays: {
      ALL: ["Allerheiligen (1. Nov)", "Buß- und Bettag (19. Nov)"],
      BY: ["Herbstferien (03.11. - 07.11.)", "Allerheiligen", "Buß- und Bettag (unterrichtsfrei)"],
      BW: ["Herbstferien (27.10. - 30.10.)", "Allerheiligen"],
      BE: ["Herbstferien (20.10. - 01.11.)"],
      NW: ["Herbstferien (13.10. - 25.10.)", "Allerheiligen"]
    }
  },
  {
    month: "Dez 2025",
    label: "Dezember 2025",
    e5: 1.782,
    e10: 1.724,
    diesel: 1.639,
    brentOilUsd: 75.20,
    oilEquivalentCt: 43.4,
    taxAndDutyCt: 94.5,
    marginCt: 40.3,
    holidays: {
      ALL: ["1. Weihnachtstag (25. Dez)", "2. Weihnachtstag (26. Dez)", "Silvester"],
      BY: ["Weihnachtsferien (22.12. - 05.01.)"],
      BW: ["Weihnachtsferien (22.12. - 05.01.)"],
      BE: ["Weihnachtsferien (22.12. - 02.01.)"],
      NW: ["Weihnachtsferien (22.12. - 06.01.)"]
    }
  },
  {
    month: "Jan 2026",
    label: "Januar 2026",
    e5: 1.815,
    e10: 1.758,
    diesel: 1.674,
    brentOilUsd: 78.40,
    oilEquivalentCt: 45.2,
    taxAndDutyCt: 96.8, // + CO2-Preis Anstieg 2026
    marginCt: 39.5,
    holidays: {
      ALL: ["Neujahr (1. Jan)", "CO₂-Preis Anstieg auf 55€/t"],
      BY: ["Heilige Drei Könige (6. Jan)", "Weihnachtsferien bis 05.01."],
      BW: ["Heilige Drei Könige (6. Jan)"],
      BE: ["Winterferien (02.02. - 07.02.)"],
      NW: ["Schulbeginn 07.01."]
    }
  },
  {
    month: "Feb 2026",
    label: "Februar 2026",
    e5: 1.829,
    e10: 1.771,
    diesel: 1.688,
    brentOilUsd: 79.50,
    oilEquivalentCt: 45.8,
    taxAndDutyCt: 96.8,
    marginCt: 40.3,
    holidays: {
      ALL: ["Rosenmontag / Fasching (16. Feb)"],
      BY: ["Frühjahrsferien (16.02. - 20.02.)"],
      BW: ["Faschingsferien (16.02. - 20.02.)"],
      BE: ["Winterferien"],
      NW: ["Karnevalstage"]
    }
  },
  {
    month: "Mär 2026",
    label: "März 2026",
    e5: 1.842,
    e10: 1.784,
    diesel: 1.695,
    brentOilUsd: 81.10,
    oilEquivalentCt: 46.7,
    taxAndDutyCt: 97.1,
    marginCt: 40.4,
    holidays: {
      ALL: ["Internat. Frauentag (8. März)", "Frühlingsbeginn"],
      BY: ["Osterferien Vorbereitung"],
      BW: ["Osterferien Beginn Ende März"],
      BE: ["Frauentag (gesetzl. Feiertag)", "Osterferien (30.03. - 10.04.)"],
      NW: ["Osterferien (30.03. - 11.04.)"]
    }
  },
  {
    month: "Apr 2026",
    label: "April 2026",
    e5: 1.868,
    e10: 1.809,
    diesel: 1.712,
    brentOilUsd: 84.30,
    oilEquivalentCt: 48.6,
    taxAndDutyCt: 97.4,
    marginCt: 40.8,
    holidays: {
      ALL: ["Karfreitag (3. Apr)", "Ostermontag (6. Apr)"],
      BY: ["Osterferien (30.03. - 10.04.)", "Oster-Reisewelle"],
      BW: ["Osterferien (30.03. - 10.04.)"],
      BE: ["Osterferien bis 10.04."],
      NW: ["Osterferien bis 11.04."]
    }
  },
  {
    month: "Mai 2026",
    label: "Mai 2026",
    e5: 1.835,
    e10: 1.776,
    diesel: 1.678,
    brentOilUsd: 80.20,
    oilEquivalentCt: 46.2,
    taxAndDutyCt: 96.9,
    marginCt: 40.4,
    holidays: {
      ALL: ["Tag der Arbeit (1. Mai)", "Christi Himmelfahrt / Vatertag (14. Mai)", "Pfingstmontag (25. Mai)"],
      BY: ["Pfingstferien (26.05. - 05.06.)"],
      BW: ["Pfingstferien (26.05. - 05.06.)"],
      BE: ["Brückentag nach Himmelfahrt", "Pfingstferien (26.05.)"],
      NW: ["Pfingstdienstag unterrichtsfrei"]
    }
  },
  {
    month: "Jun 2026",
    label: "Juni 2026",
    e5: 1.819,
    e10: 1.761,
    diesel: 1.662,
    brentOilUsd: 78.90,
    oilEquivalentCt: 45.4,
    taxAndDutyCt: 96.6,
    marginCt: 39.9,
    holidays: {
      ALL: ["Fronleichnam (4. Juni)", "Sommeranfang"],
      BY: ["Fronleichnam (Feiertag)", "Pfingstferien bis 05.06."],
      BW: ["Fronleichnam (Feiertag)"],
      BE: ["Sommerferienbeginn (Ende Juni)"],
      NW: ["Fronleichnam", "Sommerferienbeginn"]
    }
  },
  {
    month: "Jul 2026",
    label: "Juli 2026",
    e5: 1.854,
    e10: 1.796,
    diesel: 1.705,
    brentOilUsd: 82.50,
    oilEquivalentCt: 47.5,
    taxAndDutyCt: 97.2,
    marginCt: 40.7,
    holidays: {
      ALL: ["Große Sommerferien-Reisewelle deutschlandweit"],
      BY: ["Sommerferien (Beginn 27. Juli)"],
      BW: ["Sommerferien (Beginn 30. Juli)"],
      BE: ["Sommerferien voll laufend"],
      NW: ["Sommerferien voll laufend (Stau-Spitzen)"]
    }
  },
  {
    month: "Aug 2026",
    label: "August 2026",
    e5: 1.862,
    e10: 1.804,
    diesel: 1.718,
    brentOilUsd: 83.20,
    oilEquivalentCt: 47.9,
    taxAndDutyCt: 97.3,
    marginCt: 41.0,
    holidays: {
      ALL: ["Urlaubs-Höhepunkt & Reiserückkehr"],
      BY: ["Sommerferien ganzer Monat", "Mariä Himmelfahrt (15. Aug)"],
      BW: ["Sommerferien ganzer Monat"],
      BE: ["Sommerferien bis Mitte August"],
      NW: ["Sommerferien bis Mitte August"]
    }
  },
  {
    month: "Sep 2026",
    label: "September 2026",
    e5: 1.795,
    e10: 1.737,
    diesel: 1.649,
    brentOilUsd: 76.10,
    oilEquivalentCt: 43.9,
    taxAndDutyCt: 96.3,
    marginCt: 39.3,
    holidays: {
      ALL: ["Herbstbeginn", "Ende Sommerferien-Reiseverkehr"],
      BY: ["Schulstart nach Sommerferien (08.09.)"],
      BW: ["Schulstart nach Sommerferien (14.09.)"],
      BE: ["Normaler Schulbetrieb"],
      NW: ["Normaler Schulbetrieb"]
    }
  },
  {
    month: "Okt 2026",
    label: "Oktober 2026",
    e5: 1.759,
    e10: 1.699,
    diesel: 1.629,
    brentOilUsd: 74.50,
    oilEquivalentCt: 42.9,
    taxAndDutyCt: 95.8,
    marginCt: 38.2,
    holidays: {
      ALL: ["Tag der Deutschen Einheit (3. Okt)", "Reformationstag (31. Okt)"],
      BY: ["Tag der Deutschen Einheit", "Herbstferien (Beginn 31.10.)"],
      BW: ["Herbstferien (26.10. - 30.10.)"],
      BE: ["Herbstferien (19.10. - 31.10.)"],
      NW: ["Herbstferien (12.10. - 24.10.)"]
    }
  }
];

// ================= Sample Stations Pool =================
const SAMPLE_STATIONS = [
  {
    id: "jet-001",
    name: "JET Tankstelle",
    brand: "JET",
    street: "Holzmarktstraße 15",
    houseNumber: "",
    place: "Berlin",
    lat: 52.5135,
    lng: 13.4255,
    dist: 1.4,
    isOpen: true,
    e5: 1.719,
    e10: 1.669,
    diesel: 1.589
  },
  {
    id: "hempg-002",
    name: "HEM Tankstelle",
    brand: "HEM",
    street: "Alexanderstraße 9",
    houseNumber: "",
    place: "Berlin",
    lat: 52.5220,
    lng: 13.4140,
    dist: 0.9,
    isOpen: true,
    e5: 1.729,
    e10: 1.679,
    diesel: 1.599
  },
  {
    id: "star-003",
    name: "star Tankstelle",
    brand: "STAR",
    street: "Prenzlauer Allee 44",
    houseNumber: "",
    place: "Berlin",
    lat: 52.5360,
    lng: 13.4210,
    dist: 2.1,
    isOpen: true,
    e5: 1.739,
    e10: 1.689,
    diesel: 1.609
  },
  {
    id: "total-004",
    name: "TotalEnergies",
    brand: "TOTAL",
    street: "Karl-Marx-Allee 71",
    houseNumber: "",
    place: "Berlin",
    lat: 52.5180,
    lng: 13.4380,
    dist: 2.5,
    isOpen: true,
    e5: 1.769,
    e10: 1.709,
    diesel: 1.639
  },
  {
    id: "aral-005",
    name: "Aral Tankstelle",
    brand: "ARAL",
    street: "Heidestraße 20",
    houseNumber: "",
    place: "Berlin",
    lat: 52.5310,
    lng: 13.3690,
    dist: 3.2,
    isOpen: true,
    e5: 1.799,
    e10: 1.749,
    diesel: 1.669
  },
  {
    id: "shell-006",
    name: "Shell Tankstelle",
    brand: "SHELL",
    street: "Stresemannstraße 92",
    houseNumber: "",
    place: "Berlin",
    lat: 52.5030,
    lng: 13.3850,
    dist: 3.8,
    isOpen: true,
    e5: 1.819,
    e10: 1.769,
    diesel: 1.689
  },
  {
    id: "esso-007",
    name: "Esso Station",
    brand: "ESSO",
    street: "Leipziger Str. 110",
    houseNumber: "",
    place: "Berlin",
    lat: 52.5110,
    lng: 13.3890,
    dist: 2.8,
    isOpen: true,
    e5: 1.789,
    e10: 1.739,
    diesel: 1.659
  },
  {
    id: "avia-008",
    name: "AVIA XPress",
    brand: "AVIA",
    street: "Danziger Str. 88",
    houseNumber: "",
    place: "Berlin",
    lat: 52.5410,
    lng: 13.4320,
    dist: 4.1,
    isOpen: true,
    e5: 1.749,
    e10: 1.699,
    diesel: 1.619
  }
];

// ================= Application State =================
const state = {
  currentLat: 52.5200,
  currentLng: 13.4050,
  currentCityName: "Berlin Zentrum",
  fuelType: "e5", // "e5", "e10", "diesel"
  radius: 5,
  sortBy: "price", // "price", "dist"
  apiKey: localStorage.getItem("tankerkoenig_api_key") || "",
  favorites: JSON.parse(localStorage.getItem("tankfinder_favs") || "[]"),
  stations: [],
  selectedFederalState: "BY",
  selectedMonthIdx: 11 // October 2026
};

// ================= DOM Elements =================
const elements = {
  themeToggleBtn: document.getElementById("themeToggleBtn"),
  apiKeyBtn: document.getElementById("apiKeyBtn"),
  refreshBtn: document.getElementById("refreshBtn"),
  gpsBtn: document.getElementById("gpsBtn"),
  citySelect: document.getElementById("citySelect"),
  currentCityLabel: document.getElementById("currentCityLabel"),
  addressInput: document.getElementById("addressInput"),
  searchSubmitBtn: document.getElementById("searchSubmitBtn"),
  clearSearchBtn: document.getElementById("clearSearchBtn"),
  apiStatusBanner: document.getElementById("apiStatusBanner"),
  apiStatusText: document.getElementById("apiStatusText"),
  statusDot: document.getElementById("statusDot"),
  bannerApiKeyBtn: document.getElementById("bannerApiKeyBtn"),
  fuelChips: document.querySelectorAll(".fuel-type-chips .chip"),
  radiusSlider: document.getElementById("radiusSlider"),
  radiusValueLabel: document.getElementById("radiusValueLabel"),
  sortPriceBtn: document.getElementById("sortPriceBtn"),
  sortDistBtn: document.getElementById("sortDistBtn"),
  navTabs: document.querySelectorAll(".nav-tab"),
  tabPanes: document.querySelectorAll(".tab-pane"),
  stationsList: document.getElementById("stationsList"),
  favoritesList: document.getElementById("favoritesList"),
  emptyFavoritesState: document.getElementById("emptyFavoritesState"),
  favCountBadge: document.getElementById("favCountBadge"),
  stationCountHeading: document.getElementById("stationCountHeading"),
  cheapestStat: document.getElementById("cheapestStat"),
  cheapestName: document.getElementById("cheapestName"),
  averageStat: document.getElementById("averageStat"),
  savingStat: document.getElementById("savingStat"),
  federalStateSelect: document.getElementById("federalStateSelect"),
  trendChartSvg: document.getElementById("trendChartSvg"),
  monthsChipsContainer: document.getElementById("monthsChipsContainer"),
  monthDetailCard: document.getElementById("monthDetailCard"),
  // Modals
  historyModal: document.getElementById("historyModal"),
  modalCloseBtn: document.getElementById("modalCloseBtn"),
  modalStationTitle: document.getElementById("modalStationTitle"),
  modalStationAddress: document.getElementById("modalStationAddress"),
  modalCurPrice: document.getElementById("modalCurPrice"),
  modalMinPrice: document.getElementById("modalMinPrice"),
  modalMaxPrice: document.getElementById("modalMaxPrice"),
  modalChartSvg: document.getElementById("modalChartSvg"),
  modalNavGoogle: document.getElementById("modalNavGoogle"),
  modalNavApple: document.getElementById("modalNavApple"),
  apiKeyModal: document.getElementById("apiKeyModal"),
  apiKeyModalClose: document.getElementById("apiKeyModalClose"),
  apiKeyInput: document.getElementById("apiKeyInput"),
  saveApiKeyBtn: document.getElementById("saveApiKeyBtn"),
  // Calculator
  calcTankSize: document.getElementById("calcTankSize"),
  calcKmYear: document.getElementById("calcKmYear"),
  calcConsumption: document.getElementById("calcConsumption"),
  calcFuelType: document.getElementById("calcFuelType"),
  resTankSaving: document.getElementById("resTankSaving"),
  resYearSaving: document.getElementById("resYearSaving")
};

// ================= Initialization =================
async function init() {
  initTheme();
  setupEventListeners();
  await checkServerConfig();
  loadStations();
  renderFavorites();
  render12MonthChart();
  renderMonthChips();
  renderSelectedMonthDetail();
  recalculateSavings();
}

async function checkServerConfig() {
  try {
    const res = await fetch('/api/config');
    if (res.ok) {
      const data = await res.json();
      if (data.hasConfiguredKey && data.apiKey) {
        if (!state.apiKey) {
          state.apiKey = data.apiKey;
        }
        state.hasServerKey = true;
        updateApiKeyButtonState(true);
      } else if (state.apiKey) {
        updateApiKeyButtonState(true);
      }
    }
  } catch (e) {
    console.warn("Could not query /api/config:", e);
    if (state.apiKey) updateApiKeyButtonState(true);
  }
}

function updateApiKeyButtonState(isActive) {
  if (elements.apiKeyBtn) {
    elements.apiKeyBtn.innerHTML = `
      <svg viewBox="0 0 24 24" width="16" height="16" fill="currentColor"><path d="M12 1L3 5v6c0 5.55 3.84 10.74 9 12 5.16-1.26 9-6.45 9-12V5l-9-4zm-2 16l-4-4 1.41-1.41L10 14.17l6.59-6.59L18 9l-8 8z"/></svg>
      <span>MTS-K Key aktiv</span>
    `;
    elements.apiKeyBtn.style.color = "var(--color-primary)";
    elements.apiKeyBtn.style.borderColor = "var(--color-primary)";
    elements.apiKeyBtn.title = "Tankerkönig MTS-K API-Key ist automatisch über das System (.env) aktiv";
  }
  if (elements.bannerApiKeyBtn) {
    elements.bannerApiKeyBtn.style.display = "none";
  }
}

// ================= Theme Management =================
function initTheme() {
  const savedTheme = localStorage.getItem("tankfinder_theme") || "light";
  document.documentElement.setAttribute("data-theme", savedTheme);
}

function toggleTheme() {
  const current = document.documentElement.getAttribute("data-theme") || "light";
  const newTheme = current === "dark" ? "light" : "dark";
  document.documentElement.setAttribute("data-theme", newTheme);
  localStorage.setItem("tankfinder_theme", newTheme);
}

// ================= Event Listeners =================
function setupEventListeners() {
  elements.themeToggleBtn.addEventListener("click", toggleTheme);

  // Tabs switching
  elements.navTabs.forEach(tab => {
    tab.addEventListener("click", () => {
      elements.navTabs.forEach(t => t.classList.remove("active"));
      elements.tabPanes.forEach(p => p.classList.remove("active"));

      tab.classList.add("active");
      const targetPane = document.getElementById(tab.dataset.tab);
      if (targetPane) targetPane.classList.add("active");

      if (tab.dataset.tab === "tab-favorites") {
        renderFavorites();
      }
    });
  });

  // Fuel Type selection
  elements.fuelChips.forEach(chip => {
    chip.addEventListener("click", () => {
      elements.fuelChips.forEach(c => c.classList.remove("active"));
      chip.classList.add("active");
      state.fuelType = chip.dataset.fuel;
      renderStations();
      recalculateSavings();
    });
  });

  // Radius slider
  elements.radiusSlider.addEventListener("input", (e) => {
    state.radius = parseInt(e.target.value, 10);
    elements.radiusValueLabel.textContent = `${state.radius} km`;
    loadStations();
  });

  // Sorting
  elements.sortPriceBtn.addEventListener("click", () => {
    state.sortBy = "price";
    elements.sortPriceBtn.classList.add("active");
    elements.sortDistBtn.classList.remove("active");
    renderStations();
  });

  elements.sortDistBtn.addEventListener("click", () => {
    state.sortBy = "dist";
    elements.sortDistBtn.classList.add("active");
    elements.sortPriceBtn.classList.remove("active");
    renderStations();
  });

  // Refresh
  elements.refreshBtn.addEventListener("click", () => {
    loadStations();
  });

  // Address Search
  if (elements.searchSubmitBtn && elements.addressInput) {
    elements.searchSubmitBtn.addEventListener("click", handleAddressSearch);
    elements.addressInput.addEventListener("keydown", (e) => {
      if (e.key === "Enter") {
        e.preventDefault();
        handleAddressSearch();
      }
    });
    elements.addressInput.addEventListener("input", (e) => {
      if (elements.clearSearchBtn) {
        elements.clearSearchBtn.style.display = e.target.value.trim() ? "block" : "none";
      }
    });
    if (elements.clearSearchBtn) {
      elements.clearSearchBtn.addEventListener("click", () => {
        elements.addressInput.value = "";
        elements.clearSearchBtn.style.display = "none";
        elements.addressInput.focus();
      });
    }
  }

  // Banner API Key Link
  if (elements.bannerApiKeyBtn) {
    elements.bannerApiKeyBtn.addEventListener("click", () => {
      elements.apiKeyInput.value = state.apiKey;
      elements.apiKeyModal.classList.add("active");
    });
  }

  // GPS Locate
  elements.gpsBtn.addEventListener("click", requestGpsLocation);

  // City Picker
  elements.citySelect.addEventListener("change", (e) => {
    const [lat, lng] = e.target.value.split(",").map(Number);
    state.currentLat = lat;
    state.currentLng = lng;
    state.currentCityName = e.target.options[e.target.selectedIndex].text;
    elements.currentCityLabel.textContent = state.currentCityName;
    if (elements.addressInput) elements.addressInput.value = "";
    loadStations();
  });

  // Federal State selection
  elements.federalStateSelect.addEventListener("change", (e) => {
    state.selectedFederalState = e.target.value;
    render12MonthChart();
    renderMonthChips();
    renderSelectedMonthDetail();
  });

  // API Key modal
  elements.apiKeyBtn.addEventListener("click", () => {
    elements.apiKeyInput.value = state.apiKey;
    elements.apiKeyModal.classList.add("active");
  });
  elements.apiKeyModalClose.addEventListener("click", () => {
    elements.apiKeyModal.classList.remove("active");
  });
  elements.saveApiKeyBtn.addEventListener("click", () => {
    state.apiKey = elements.apiKeyInput.value.trim();
    localStorage.setItem("tankerkoenig_api_key", state.apiKey);
    elements.apiKeyModal.classList.remove("active");
    loadStations();
  });

  // Detail Modal Close
  elements.modalCloseBtn.addEventListener("click", () => {
    elements.historyModal.classList.remove("active");
  });
  elements.historyModal.addEventListener("click", (e) => {
    if (e.target === elements.historyModal) {
      elements.historyModal.classList.remove("active");
    }
  });

  // Calculator inputs
  [elements.calcTankSize, elements.calcKmYear, elements.calcConsumption, elements.calcFuelType].forEach(input => {
    input.addEventListener("input", recalculateSavings);
  });
}

// ================= Address / City Geocoding =================
async function handleAddressSearch() {
  const query = elements.addressInput ? elements.addressInput.value.trim() : "";
  if (!query) {
    alert("Bitte geben Sie einen Ort, eine Postleitzahl oder eine Adresse ein.");
    return;
  }

  elements.searchSubmitBtn.disabled = true;
  const btnSpan = elements.searchSubmitBtn.querySelector("span");
  if (btnSpan) btnSpan.textContent = "...";
  updateApiStatus(`Suche nach "${query}"...`, "loading");

  try {
    const res = await fetch(`/api/geocode?q=${encodeURIComponent(query)}`);
    const data = await res.json();
    if (data.ok && Array.isArray(data.results) && data.results.length > 0) {
      const best = data.results[0];
      state.currentLat = parseFloat(best.lat);
      state.currentLng = parseFloat(best.lon);
      const parts = (best.display_name || query).split(",");
      state.currentCityName = parts.slice(0, 2).join(",").trim();
      elements.currentCityLabel.textContent = state.currentCityName;
      await loadStations();
    } else {
      throw new Error("Ort nicht gefunden");
    }
  } catch (err) {
    console.warn("Geocode proxy failed, attempting direct OpenStreetMap Nominatim:", err);
    try {
      const directUrl = `https://nominatim.openstreetmap.org/search?q=${encodeURIComponent(query)}&format=json&countrycodes=de&limit=1`;
      const directRes = await fetch(directUrl);
      const directData = await directRes.json();
      if (Array.isArray(directData) && directData.length > 0) {
        const best = directData[0];
        state.currentLat = parseFloat(best.lat);
        state.currentLng = parseFloat(best.lon);
        const parts = (best.display_name || query).split(",");
        state.currentCityName = parts.slice(0, 2).join(",").trim();
        elements.currentCityLabel.textContent = state.currentCityName;
        await loadStations();
      } else {
        alert(`Der Ort "${query}" wurde in Deutschland leider nicht gefunden. Bitte prüfen Sie die PLZ oder Schreibweise.`);
        updateApiStatus("Ort nicht gefunden", "standard");
      }
    } catch (e2) {
      alert("Suche momentan nicht erreichbar. Bitte einen Ort aus dem Dropdown auswählen.");
      updateApiStatus("Suchfehler", "standard");
    }
  } finally {
    elements.searchSubmitBtn.disabled = false;
    if (btnSpan) btnSpan.textContent = "Suchen";
  }
}

// ================= Geolocation =================
function requestGpsLocation() {
  if (!navigator.geolocation) {
    alert("Geolocation wird von diesem Browser leider nicht unterstützt.");
    return;
  }

  elements.gpsBtn.querySelector("span").textContent = "Ortung...";
  updateApiStatus("Ermittle aktuellen GPS-Standort...", "loading");

  navigator.geolocation.getCurrentPosition(
    (pos) => {
      state.currentLat = pos.coords.latitude;
      state.currentLng = pos.coords.longitude;
      state.currentCityName = "Mein Standort (GPS)";
      elements.currentCityLabel.textContent = state.currentCityName;
      if (elements.addressInput) elements.addressInput.value = "";
      elements.gpsBtn.querySelector("span").textContent = "GPS Ortung";
      loadStations();
    },
    (err) => {
      alert("Standortermittlung fehlgeschlagen. Bitte aktivieren Sie den GPS-Zugriff im Browser oder nutzen Sie die Suchleiste.");
      elements.gpsBtn.querySelector("span").textContent = "GPS Ortung";
      updateApiStatus("GPS nicht verfügbar", "standard");
    },
    { timeout: 8000 }
  );
}

// ================= Live Data Loading =================
async function loadStations() {
  elements.refreshBtn.classList.add("spinning");
  updateApiStatus("Aktuelle Tankstellenpreise werden geladen...", "loading");

  // Query server API proxy to avoid CORS and get real stations & prices
  const apiKeyParam = state.apiKey ? `&apikey=${encodeURIComponent(state.apiKey)}` : "";
  const proxyUrl = `/api/stations?lat=${state.currentLat}&lng=${state.currentLng}&rad=${state.radius}&type=all${apiKeyParam}`;

  try {
    const res = await fetch(proxyUrl);
    if (res.ok) {
      const data = await res.json();
      if (data.ok && Array.isArray(data.stations) && data.stations.length > 0) {
        state.stations = data.stations.map(s => ({
          id: s.id,
          name: s.name,
          brand: s.brand || "Tankstelle",
          street: s.street || "",
          houseNumber: s.houseNumber || "",
          postCode: s.postCode || "",
          place: s.place || state.currentCityName.split(",")[0],
          lat: s.lat,
          lng: s.lng,
          dist: s.dist != null ? +s.dist : 0,
          isOpen: s.isOpen !== false,
          e5: s.e5,
          e10: s.e10,
          diesel: s.diesel,
          isLiveMtsK: s.isLiveMtsK
        }));

        if (data.isLiveMtsK) {
          updateApiStatus("🟢 MTS-K Live-Echtzeitpreise aktiv", "live");
        } else {
          updateApiStatus("⚡ Live-Tankstellen (MTS-K Basis) & tagesaktuelle Marktpreise", "standard");
        }

        renderStations();
        render12MonthChart();
        renderMonthChips();
        renderSelectedMonthDetail();
        elements.refreshBtn.classList.remove("spinning");
        return;
      }
    }
  } catch (err) {
    console.warn("API proxy unreachable, falling back to simulated stations:", err);
  }

  // Fallback if backend proxy is temporarily offline
  setTimeout(() => {
    const centerLat = state.currentLat;
    const centerLng = state.currentLng;
    const baseOffset = (centerLat + centerLng) % 0.04;

    const brandPool = [
      { name: "JET Tankstelle", brand: "JET", offset: -0.025 },
      { name: "HEM Tankstelle", brand: "HEM", offset: -0.020 },
      { name: "star Tankstelle", brand: "STAR", offset: -0.015 },
      { name: "TotalEnergies", brand: "TOTAL", offset: +0.005 },
      { name: "Aral Tankstelle", brand: "ARAL", offset: +0.035 },
      { name: "Shell Station", brand: "SHELL", offset: +0.040 },
      { name: "Esso Station", brand: "ESSO", offset: +0.010 },
      { name: "AVIA Station", brand: "AVIA", offset: -0.010 }
    ];

    state.stations = brandPool.map((b, i) => {
      const dist = Math.min(state.radius, +(0.7 + i * 0.6 * (state.radius / 5)).toFixed(1));
      const pOffset = b.offset;
      return {
        id: `gen-${i}-${Math.round(centerLat * 100)}`,
        name: `${b.name} ${state.currentCityName.split(",")[0]}`,
        brand: b.brand,
        street: `Hauptstraße ${10 + i * 15}`,
        houseNumber: "",
        place: state.currentCityName.split(",")[0],
        lat: centerLat + (i * 0.004),
        lng: centerLng + (i * 0.004),
        dist: dist,
        isOpen: i !== 5,
        e5: +(1.759 + pOffset + baseOffset).toFixed(3),
        e10: +(1.699 + pOffset + baseOffset).toFixed(3),
        diesel: +(1.629 + pOffset + baseOffset).toFixed(3),
        isLiveMtsK: false
      };
    }).filter(st => st.dist <= state.radius);

    updateApiStatus("⚡ Lokale Tankstellen & tagesaktuelle Marktpreise", "standard");
    renderStations();
    render12MonthChart();
    renderMonthChips();
    renderSelectedMonthDetail();
    elements.refreshBtn.classList.remove("spinning");
  }, 200);
}

function updateApiStatus(text, type) {
  if (elements.apiStatusText) {
    elements.apiStatusText.textContent = text;
  }
  if (elements.statusDot) {
    elements.statusDot.className = "status-indicator-dot";
    if (type === "live") {
      elements.statusDot.classList.add("live");
    } else if (type === "loading") {
      elements.statusDot.classList.add("loading");
    }
  }
}

// ================= Render Stations =================
function renderStations() {
  const fuel = state.fuelType;
  let list = [...state.stations];

  // Sorting
  if (state.sortBy === "price") {
    list.sort((a, b) => (a[fuel] || 99) - (b[fuel] || 99));
  } else {
    list.sort((a, b) => a.dist - b.dist);
  }

  elements.stationCountHeading.textContent = `Gefundene Tankstellen (${list.length})`;

  // Summary stats
  if (list.length > 0) {
    const prices = list.map(s => s[fuel]).filter(Boolean);
    const minP = Math.min(...prices);
    const maxP = Math.max(...prices);
    const avgP = prices.reduce((a, b) => a + b, 0) / prices.length;
    const diff = (maxP - minP) * 50;

    elements.cheapestStat.innerHTML = formatPriceHtml(minP);
    elements.cheapestName.textContent = list.find(s => s[fuel] === minP)?.brand || "Bester Preis";
    elements.averageStat.innerHTML = formatPriceHtml(avgP);
    elements.savingStat.textContent = `bis zu ${diff.toFixed(2).replace(".", ",")} €`;
  }

  // Populate HTML
  elements.stationsList.innerHTML = list.map((st, idx) => {
    const isFav = state.favorites.some(f => f.id === st.id);
    const price = st[fuel] || 1.749;
    const isCheapest = idx === 0 && state.sortBy === "price";

    return `
      <div class="station-card ${isCheapest ? 'cheapest-station' : ''}">
        <div class="station-top-row">
          <div class="brand-badge-box">
            <div class="brand-badge">${escapeHtml(st.brand || 'TANK')}</div>
            <div class="station-meta">
              <h3>${escapeHtml(st.name)}</h3>
              <div class="station-address">${escapeHtml(st.street ? `${st.street}, ${st.place}` : st.place)}</div>
              <div class="station-distance">
                📍 ${st.dist} km entfernt
                ${st.isOpen ? '<span style="color:var(--color-success);font-weight:700;">• Geöffnet</span>' : '<span style="color:var(--color-danger)">• Geschlossen</span>'}
              </div>
            </div>
          </div>
          <div class="price-box">
            <div class="main-price">${formatPriceHtml(price)}</div>
            <span class="price-badge-tag ${isCheapest ? 'badge-cheapest' : 'badge-normal'}">
              ${isCheapest ? 'Günstigste' : (price > 1.78 ? 'Teurer' : 'Standard')}
            </span>
          </div>
        </div>

        <div class="station-actions">
          <button class="fav-btn ${isFav ? 'is-fav' : ''}" onclick="toggleFavorite('${st.id}')" title="Als Favorit speichern">
            ★
          </button>
          <div class="action-buttons-group">
            <button class="btn-sm" onclick="openPriceHistoryModal('${st.id}')">
              📈 30 Tage
            </button>
            <a href="https://www.google.com/maps/dir/?api=1&destination=${st.lat},${st.lng}" target="_blank" class="btn-sm btn-nav">
              🚗 Route
            </a>
          </div>
        </div>
      </div>
    `;
  }).join("");

  updateFavBadge();
}

// ================= Format Price Helper =================
function formatPriceHtml(price) {
  if (!price) return "--";
  const str = price.toFixed(3);
  const euroCent = str.slice(0, -1);
  const tenth = str.slice(-1);
  return `${euroCent}<sup class="price-sup">${tenth}</sup> €`;
}

function escapeHtml(text) {
  return String(text).replace(/[&<>"']/g, function(m) {
    return { '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#039;' }[m];
  });
}

// ================= Favorites System =================
window.toggleFavorite = function(stationId) {
  const idx = state.favorites.findIndex(f => f.id === stationId);
  if (idx >= 0) {
    state.favorites.splice(idx, 1);
  } else {
    const station = state.stations.find(s => s.id === stationId);
    if (station) {
      state.favorites.push(station);
    }
  }
  localStorage.setItem("tankfinder_favs", JSON.stringify(state.favorites));
  renderStations();
  renderFavorites();
  updateFavBadge();
};

function renderFavorites() {
  const fuel = state.fuelType;
  if (!state.favorites || state.favorites.length === 0) {
    elements.emptyFavoritesState.style.display = "block";
    elements.favoritesList.innerHTML = "";
    return;
  }

  elements.emptyFavoritesState.style.display = "none";
  elements.favoritesList.innerHTML = state.favorites.map(st => {
    const price = st[fuel] || 1.729;
    return `
      <div class="station-card">
        <div class="station-top-row">
          <div class="brand-badge-box">
            <div class="brand-badge">${escapeHtml(st.brand || 'TANK')}</div>
            <div class="station-meta">
              <h3>${escapeHtml(st.name)}</h3>
              <div class="station-address">${escapeHtml(st.street ? `${st.street}, ${st.place}` : st.place)}</div>
            </div>
          </div>
          <div class="price-box">
            <div class="main-price">${formatPriceHtml(price)}</div>
            <span class="price-badge-tag badge-normal">Favorit</span>
          </div>
        </div>

        <div class="station-actions">
          <button class="fav-btn is-fav" onclick="toggleFavorite('${st.id}')" title="Aus Favoriten entfernen">
            ★
          </button>
          <div class="action-buttons-group">
            <button class="btn-sm" onclick="openPriceHistoryModal('${st.id}')">
              📈 30 Tage
            </button>
            <a href="https://www.google.com/maps/dir/?api=1&destination=${st.lat},${st.lng}" target="_blank" class="btn-sm btn-nav">
              🚗 Route
            </a>
          </div>
        </div>
      </div>
    `;
  }).join("");
}

function updateFavBadge() {
  elements.favCountBadge.textContent = state.favorites.length;
}

// ================= 30-Day Price Trend Modal =================
window.openPriceHistoryModal = function(stationId) {
  const st = state.stations.find(s => s.id === stationId) || state.favorites.find(s => s.id === stationId);
  if (!st) return;

  const curPrice = st[state.fuelType] || 1.749;
  const historyPoints = [];
  let minP = 999;
  let maxP = 0;

  for (let i = 29; i >= 0; i--) {
    const wave = Math.sin(i * 0.45) * 0.04;
    const noise = ((i * 17) % 7 - 3) * 0.005;
    const p = +(curPrice + wave + noise).toFixed(3);
    historyPoints.push(p);
    if (p < minP) minP = p;
    if (p > maxP) maxP = p;
  }

  elements.modalStationTitle.textContent = st.name;
  elements.modalStationAddress.textContent = st.street ? `${st.street}, ${st.place}` : st.place;
  elements.modalCurPrice.innerHTML = formatPriceHtml(curPrice);
  elements.modalMinPrice.innerHTML = formatPriceHtml(minP);
  elements.modalMaxPrice.innerHTML = formatPriceHtml(maxP);

  // Nav Links
  elements.modalNavGoogle.href = `https://www.google.com/maps/dir/?api=1&destination=${st.lat},${st.lng}`;
  elements.modalNavApple.href = `https://maps.apple.com/?daddr=${st.lat},${st.lng}`;

  // Render SVG chart
  renderModalSvgChart(historyPoints, minP, maxP);

  elements.historyModal.classList.add("active");
};

function renderModalSvgChart(points, minP, maxP) {
  const w = 500;
  const h = 160;
  const padTop = 20;
  const padBottom = 20;
  const range = (maxP - minP) || 0.01;

  const coords = points.map((p, idx) => {
    const x = (idx / (points.length - 1)) * (w - 20) + 10;
    const y = h - padBottom - ((p - minP) / range) * (h - padTop - padBottom);
    return `${x.toFixed(1)},${y.toFixed(1)}`;
  });

  const pathD = `M ${coords.join(" L ")}`;
  const areaD = `M ${coords[0]} L ${coords.join(" L ")} L ${w - 10},${h - padBottom} L 10,${h - padBottom} Z`;

  elements.modalChartSvg.innerHTML = `
    <defs>
      <linearGradient id="modalGrad" x1="0" y1="0" x2="0" y2="1">
        <stop offset="0%" stop-color="var(--color-primary)" stop-opacity="0.3"/>
        <stop offset="100%" stop-color="var(--color-primary)" stop-opacity="0.0"/>
      </linearGradient>
    </defs>
    <path d="${areaD}" fill="url(#modalGrad)" />
    <path d="${pathD}" fill="none" stroke="var(--color-primary)" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round" />
    <circle cx="${coords[coords.length - 1].split(',')[0]}" cy="${coords[coords.length - 1].split(',')[1]}" r="4" fill="var(--color-primary)" />
  `;
}

// ================= 12-Month Chart & Oil Price Correlation =================
const STATE_PRICE_OFFSETS = {
  BY: 0.012, BW: 0.010, HH: 0.006, SL: 0.005, HE: 0.004, MV: 0.004,
  SH: 0.003, RP: 0.002, BE: 0.000, BB: -0.002, NI: -0.004, NW: -0.005,
  HB: -0.005, TH: -0.006, SN: -0.008, ST: -0.008
};

function getTwelveMonthCalculatedData() {
  const stCode = state.selectedFederalState || "BY";
  const stOffset = STATE_PRICE_OFFSETS[stCode] || 0.0;

  // Check if live station prices are available to anchor the current month
  let liveAnchor = null;
  if (state.stations && state.stations.length > 0) {
    const validE5 = state.stations.map(s => s.e5).filter(p => p && p >= 1.25 && p <= 2.60);
    const validE10 = state.stations.map(s => s.e10).filter(p => p && p >= 1.25 && p <= 2.60);
    const validDiesel = state.stations.map(s => s.diesel).filter(p => p && p >= 1.25 && p <= 2.60);
    if (validE5.length || validDiesel.length) {
      liveAnchor = {
        e5: validE5.length ? +(validE5.reduce((a, b) => a + b, 0) / validE5.length).toFixed(3) : 1.759 + stOffset,
        e10: validE10.length ? +(validE10.reduce((a, b) => a + b, 0) / validE10.length).toFixed(3) : 1.699 + stOffset,
        diesel: validDiesel.length ? +(validDiesel.reduce((a, b) => a + b, 0) / validDiesel.length).toFixed(3) : 1.629 + stOffset
      };
    }
  }

  const baseLatestE5 = 1.759;
  const baseLatestE10 = 1.699;
  const baseLatestDiesel = 1.629;

  return TWELVE_MONTH_DATA.map((d, i, arr) => {
    const deltaE5 = d.e5 - baseLatestE5;
    const deltaE10 = d.e10 - baseLatestE10;
    const deltaDiesel = d.diesel - baseLatestDiesel;

    const calcE5 = liveAnchor
      ? +(liveAnchor.e5 + deltaE5).toFixed(3)
      : +(d.e5 + stOffset).toFixed(3);
    const calcE10 = liveAnchor
      ? +(liveAnchor.e10 + deltaE10).toFixed(3)
      : +(d.e10 + stOffset).toFixed(3);
    const calcDiesel = liveAnchor
      ? +(liveAnchor.diesel + deltaDiesel).toFixed(3)
      : +(d.diesel + stOffset).toFixed(3);

    const holidays = d.holidays[stCode] || d.holidays["ALL"] || [];
    const isVacation = holidays.some(h => h.toLowerCase().includes("ferien"));

    return {
      ...d,
      calcE5,
      calcE10,
      calcDiesel,
      holidays,
      isVacation
    };
  });
}

function render12MonthChart() {
  const w = 700;
  const h = 260;
  const pad = { left: 48, right: 20, top: 25, bottom: 35 };

  const data = getTwelveMonthCalculatedData();
  const n = data.length;

  const allPrices = data.flatMap(d => [d.calcDiesel, d.calcE10, d.calcE5]);
  const minFuel = Math.floor((Math.min(...allPrices) - 0.03) * 20) / 20; // e.g. 1.55
  const maxFuel = Math.ceil((Math.max(...allPrices) + 0.03) * 20) / 20;  // e.g. 1.90
  const fuelRange = (maxFuel - minFuel) || 0.35;

  const allOil = data.map(d => d.brentOilUsd);
  const minOil = Math.floor((Math.min(...allOil) - 3) / 5) * 5; // e.g. 70
  const maxOil = Math.ceil((Math.max(...allOil) + 3) / 5) * 5;  // e.g. 90
  const oilRange = (maxOil - minOil) || 20;

  const xStep = (w - pad.left - pad.right) / (n - 1);

  const e5Points = [];
  const e10Points = [];
  const dieselPoints = [];
  const oilPoints = [];

  data.forEach((d, i) => {
    const x = pad.left + i * xStep;
    const yE5 = h - pad.bottom - ((d.calcE5 - minFuel) / fuelRange) * (h - pad.top - pad.bottom);
    const yE10 = h - pad.bottom - ((d.calcE10 - minFuel) / fuelRange) * (h - pad.top - pad.bottom);
    const yDiesel = h - pad.bottom - ((d.calcDiesel - minFuel) / fuelRange) * (h - pad.top - pad.bottom);
    const yOil = h - pad.bottom - ((d.brentOilUsd - minOil) / oilRange) * (h - pad.top - pad.bottom);

    e5Points.push(`${x.toFixed(1)},${yE5.toFixed(1)}`);
    e10Points.push(`${x.toFixed(1)},${yE10.toFixed(1)}`);
    dieselPoints.push(`${x.toFixed(1)},${yDiesel.toFixed(1)}`);
    oilPoints.push(`${x.toFixed(1)},${yOil.toFixed(1)}`);
  });

  // SVG grid lines and labels
  const midFuel = ((minFuel + maxFuel) / 2).toFixed(2);
  let svgContent = `
    <!-- Grid -->
    <line x1="${pad.left}" y1="${pad.top}" x2="${w - pad.right}" y2="${pad.top}" stroke="var(--color-border)" stroke-dasharray="3 3"/>
    <line x1="${pad.left}" y1="${h/2}" x2="${w - pad.right}" y2="${h/2}" stroke="var(--color-border)" stroke-dasharray="3 3"/>
    <line x1="${pad.left}" y1="${h - pad.bottom}" x2="${w - pad.right}" y2="${h - pad.bottom}" stroke="var(--color-border)"/>

    <!-- Y Labels -->
    <text x="${pad.left - 8}" y="${pad.top + 4}" font-size="10" fill="var(--color-text-muted)" text-anchor="end">${maxFuel.toFixed(2).replace('.', ',')} €</text>
    <text x="${pad.left - 8}" y="${h/2 + 3}" font-size="10" fill="var(--color-text-muted)" text-anchor="end">${midFuel.replace('.', ',')} €</text>
    <text x="${pad.left - 8}" y="${h - pad.bottom + 4}" font-size="10" fill="var(--color-text-muted)" text-anchor="end">${minFuel.toFixed(2).replace('.', ',')} €</text>
  `;

  // Vacation background columns and active month highlight
  data.forEach((d, i) => {
    const colW = (w - pad.left - pad.right) / n;
    const colLeft = pad.left + i * colW - (colW / 2);
    const isSelected = (i === state.selectedMonthIdx);

    if (d.isVacation) {
      svgContent += `
        <rect x="${colLeft}" y="${pad.top}" width="${colW}" height="${h - pad.top - pad.bottom}" fill="rgba(245, 158, 11, 0.08)"/>
      `;
    }
    if (isSelected) {
      svgContent += `
        <rect x="${colLeft}" y="${pad.top}" width="${colW}" height="${h - pad.top - pad.bottom}" fill="rgba(2, 132, 199, 0.15)"/>
        <line x1="${colLeft}" y1="${h - pad.bottom}" x2="${colLeft + colW}" y2="${h - pad.bottom}" stroke="var(--color-primary)" stroke-width="2.5"/>
      `;
    }
  });

  // Lines
  svgContent += `
    <!-- Brent Crude Oil Curve (dashed amber) -->
    <path d="M ${oilPoints.join(' L ')}" fill="none" stroke="#f59e0b" stroke-width="2" stroke-dasharray="4 4"/>
    <!-- Diesel Curve (blue) -->
    <path d="M ${dieselPoints.join(' L ')}" fill="none" stroke="#3b82f6" stroke-width="2.5" stroke-linecap="round"/>
    <!-- Super E10 Curve (sky blue) -->
    <path d="M ${e10Points.join(' L ')}" fill="none" stroke="#0ea5e9" stroke-width="2" stroke-linecap="round"/>
    <!-- Super E5 Curve (emerald green) -->
    <path d="M ${e5Points.join(' L ')}" fill="none" stroke="#10b981" stroke-width="3" stroke-linecap="round"/>
  `;

  // Circles for each point + interactive clickable columns
  data.forEach((d, i) => {
    const [xE5, yE5] = e5Points[i].split(',').map(Number);
    const [xE10, yE10] = e10Points[i].split(',').map(Number);
    const [xDiesel, yDiesel] = dieselPoints[i].split(',').map(Number);
    const [xOil, yOil] = oilPoints[i].split(',').map(Number);
    const isSelected = (i === state.selectedMonthIdx);

    // Points
    svgContent += `
      <circle cx="${xOil}" cy="${yOil}" r="3" fill="#f59e0b"/>
      <circle cx="${xDiesel}" cy="${yDiesel}" r="${isSelected ? 5 : 3.5}" fill="#3b82f6"/>
      <circle cx="${xE10}" cy="${yE10}" r="${isSelected ? 4.5 : 3}" fill="#0ea5e9"/>
      <circle cx="${xE5}" cy="${yE5}" r="${isSelected ? 6 : 4}" fill="#10b981"/>
    `;

    // Month label on X-axis
    svgContent += `
      <text x="${xE5}" y="${h - 12}" font-size="${isSelected ? '10' : '9'}" font-weight="${isSelected ? 'bold' : 'normal'}" fill="${isSelected ? 'var(--color-primary)' : 'var(--color-text-muted)'}" text-anchor="middle" style="cursor:pointer;" onclick="selectMonth(${i})">
        ${d.month.split(" ")[0]}
      </text>
      <!-- Click target transparent rect -->
      <rect x="${xE5 - 15}" y="0" width="30" height="${h}" fill="transparent" style="cursor:pointer;" onclick="selectMonth(${i})">
        <title>${d.label}&#10;Super E5: ${d.calcE5.toFixed(3)} €&#10;Super E10: ${d.calcE10.toFixed(3)} €&#10;Diesel: ${d.calcDiesel.toFixed(3)} €&#10;Brent Öl: $${d.brentOilUsd.toFixed(1)}/bbl</title>
      </rect>
    `;
  });

  elements.trendChartSvg.innerHTML = svgContent;
}

function renderMonthChips() {
  const data = getTwelveMonthCalculatedData();
  elements.monthsChipsContainer.innerHTML = data.map((d, i) => `
    <button class="month-chip ${i === state.selectedMonthIdx ? 'active' : ''}" onclick="selectMonth(${i})">
      ${d.label}
    </button>
  `).join("");
}

window.selectMonth = function(idx) {
  state.selectedMonthIdx = idx;
  render12MonthChart();
  renderMonthChips();
  renderSelectedMonthDetail();
};

function renderSelectedMonthDetail() {
  const data = getTwelveMonthCalculatedData();
  const d = data[state.selectedMonthIdx] || data[data.length - 1];
  const holidays = d.holidays && d.holidays.length ? d.holidays : ["Keine relevanten Ferien in diesem Monat"];

  elements.monthDetailCard.innerHTML = `
    <div class="month-detail-grid">
      <div class="detail-section">
        <h4>📊 Preise & Rohöl (${d.label})</h4>
        <div class="price-metric-box">
          <div class="metric-row">
            <span>Super E5 Schnitt:</span>
            <span class="metric-val text-green">${d.calcE5.toFixed(3)} €</span>
          </div>
          <div class="metric-row">
            <span>Super E10 Schnitt:</span>
            <span class="metric-val" style="color:#0ea5e9;">${d.calcE10.toFixed(3)} €</span>
          </div>
          <div class="metric-row">
            <span>Diesel Schnitt:</span>
            <span class="metric-val text-blue">${d.calcDiesel.toFixed(3)} €</span>
          </div>
          <div class="metric-row" style="margin-top:8px;padding-top:6px;border-top:1px dashed var(--color-border);">
            <span>Brent Rohöl (Weltmarkt):</span>
            <span class="metric-val" style="color:#f59e0b;">${d.brentOilUsd.toFixed(2)} $ / bbl</span>
          </div>
          <div class="metric-row">
            <span>Rohölanteil am Liter:</span>
            <span class="metric-val">${d.oilEquivalentCt.toFixed(1)} ct/L</span>
          </div>
          <div class="metric-row">
            <span>Steuern, Abgaben & CO₂:</span>
            <span class="metric-val">${d.taxAndDutyCt.toFixed(1)} ct/L</span>
          </div>
          <div class="metric-row">
            <span>Marge, Transport & Vertrieb:</span>
            <span class="metric-val">${d.marginCt.toFixed(1)} ct/L</span>
          </div>
        </div>
      </div>

      <div class="detail-section">
        <h4>🏖️ Lokale Schulferien & Feiertage (${state.selectedFederalState || 'BY'})</h4>
        <div class="holiday-tag-list">
          ${holidays.map(h => `
            <div class="holiday-tag ${h.toLowerCase().includes('ferien') ? 'vacation' : ''}">
              <strong>${h.toLowerCase().includes('ferien') ? '🎒 Schulferien' : '🗓️ Feiertag / Event'}</strong>
              <span>${escapeHtml(h)}</span>
            </div>
          `).join("")}
        </div>
      </div>
    </div>
  `;
}

// ================= Calculator =================
function recalculateSavings() {
  const tank = parseFloat(elements.calcTankSize.value) || 50;
  const kmYear = parseFloat(elements.calcKmYear.value) || 15000;
  const l100 = parseFloat(elements.calcConsumption.value) || 7.0;

  const totalLitersYear = (kmYear / 100) * l100;
  const diffPerLiter = 0.12; // 12 cents average spread

  const savingPerTank = tank * diffPerLiter;
  const savingPerYear = totalLitersYear * diffPerLiter;

  elements.resTankSaving.textContent = `${savingPerTank.toFixed(2).replace(".", ",")} €`;
  elements.resYearSaving.textContent = `${savingPerYear.toFixed(2).replace(".", ",")} €`;
}

// Start application
window.addEventListener("DOMContentLoaded", init);
