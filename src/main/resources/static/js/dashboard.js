// Dashboard JavaScript

// Funkcija za inicijalizaciju stranice
document.addEventListener('DOMContentLoaded', function() {
    initWaterAnimation();
    setupEventListeners();
    setupCharts();
    initModals();
    setDefaultDate();
});

// Funkcija za postavljanje event listenera
function setupEventListeners() {
    // Event listener za odabir lokacije u glavnom sučelju
    const locationSelect = document.getElementById('locationSelect');
    if (locationSelect) {
        locationSelect.addEventListener('change', function(event) {
            // Provjeri je li ovo element u modalnom prozoru
            if (this.closest('#addConsumptionModal')) {
                // Ovo je padajući izbornik u modalnom prozoru za dodavanje potrošnje
                const locationId = this.value;
                if (locationId) {
                    loadMeasurePoints(locationId, 'measurePointSelect');
                }
            } else {
                // Ovo je padajući izbornik u glavnom sučelju
                const locationId = this.value;
                if (locationId) {
                    window.location.href = '/dashboard?locationId=' + locationId;
                } else {
                    window.location.href = '/dashboard';
                }
            }
        });
    }

    // Event listener za odabir mjernog mjesta
    const measurePointSelect = document.getElementById('measurePointSelect');
    if (measurePointSelect) {
        measurePointSelect.addEventListener('change', function(event) {
            // Provjeri je li ovo element u modalnom prozoru
            if (this.closest('#addConsumptionModal')) {
                // Ovo je padajući izbornik u modalnom prozoru za dodavanje potrošnje
                // Ne radimo ništa posebno
            } else {
                // Ovo je padajući izbornik u glavnom sučelju
                const locationId = document.getElementById('locationSelect').value;
                const measurePointId = this.value;
                if (measurePointId) {
                    window.location.href = '/dashboard?locationId=' + locationId + '&measurePointId=' + measurePointId;
                } else {
                    window.location.href = '/dashboard?locationId=' + locationId;
                }
            }
        });
    }

    // Event listener za gumbe za upravljanje mjernim mjestima
    const manageMeasurePointsButtons = document.querySelectorAll('.manage-measure-points-btn');
    manageMeasurePointsButtons.forEach(button => {
        button.addEventListener('click', function(event) {
            event.preventDefault();
            event.stopPropagation();
            
            const locationId = this.getAttribute('data-location-id');
            const locationName = this.getAttribute('data-location-name');
            
            // Postavi naslov modalnog prozora
            const modalTitle = document.getElementById('manageMeasurePointsModalLabel');
            if (modalTitle) {
                modalTitle.textContent = 'Mjerna mjesta - ' + locationName;
            }
            
            // Učitaj mjerna mjesta za ovu lokaciju
            refreshMeasurePointsTable(locationId);
            
            // Postavi ID lokacije u formu za dodavanje mjernog mjesta
            document.getElementById('addMeasurePointLocationId').value = locationId;
            
            // Prikaži modalni prozor
            const modal = new bootstrap.Modal(document.getElementById('manageMeasurePointsModal'));
            modal.show();
        });
    });

    // Event listener za formu za dodavanje mjernog mjesta
    const addMeasurePointForm = document.getElementById('addMeasurePointFormElement');
    if (addMeasurePointForm) {
        addMeasurePointForm.addEventListener('submit', function(event) {
            event.preventDefault();
            
            const locationId = document.getElementById('addMeasurePointLocationId').value;
            const name = document.getElementById('measurePointName').value;
            const description = document.getElementById('measurePointDescription').value;
            
            // Provjeri jesu li uneseni svi potrebni podaci
            if (!name) {
                alert('Molimo unesite naziv mjernog mjesta.');
                return;
            }
            
            // Pošalji zahtjev za dodavanje mjernog mjesta
            fetch('/api/measure-points', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({
                    locationId: locationId,
                    name: name,
                    description: description
                })
            })
            .then(response => {
                if (!response.ok) {
                    throw new Error('Greška pri dodavanju mjernog mjesta.');
                }
                return response.json();
            })
            .then(data => {
                // Resetiraj formu
                addMeasurePointForm.reset();
                
                // Osvježi prikaz mjernih mjesta
                refreshMeasurePointsTable(locationId);
            })
            .catch(error => {
                console.error('Error adding measure point:', error);
                alert('Greška pri dodavanju mjernog mjesta.');
            });
        });
    }

    // Event listener za odabir lokacije u modalnom prozoru za uređivanje potrošnje
    const editLocationSelect = document.getElementById('editLocationSelect');
    if (editLocationSelect) {
        editLocationSelect.addEventListener('change', function() {
            const locationId = this.value;
            if (locationId) {
                loadMeasurePoints(locationId, 'editMeasurePointSelect');
            } else {
                const measurePointSelect = document.getElementById('editMeasurePointSelect');
                measurePointSelect.innerHTML = '<option value="">Odaberite mjerno mjesto</option>';
                measurePointSelect.disabled = true;
            }
        });
    }

    // Event listeneri za gumbe za uređivanje potrošnje
    const editButtons = document.querySelectorAll('.edit-consumption-btn');
    editButtons.forEach(button => {
        button.addEventListener('click', function() {
            const id = this.getAttribute('data-id');
            const date = this.getAttribute('data-date');
            const locationId = this.getAttribute('data-location-id');
            const measurePointId = this.getAttribute('data-measure-point-id');
            const meterReading = this.getAttribute('data-meter-reading');
            const hotWaterMeterReading = this.getAttribute('data-hot-water-meter-reading');
            const coldWaterMeterReading = this.getAttribute('data-cold-water-meter-reading');
            const unit = this.getAttribute('data-unit');
            const notes = this.getAttribute('data-notes');

            // Popuni formu za uređivanje
            document.getElementById('editConsumptionId').value = id;
            document.getElementById('editConsumptionDate').value = date;
            document.getElementById('editLocationSelect').value = locationId;
            
            // Učitaj mjerna mjesta za odabranu lokaciju
            loadMeasurePoints(locationId, 'editMeasurePointSelect', measurePointId);
            
            // Postavi ostale vrijednosti
            if (meterReading) {
                document.getElementById('editMeterReading').value = meterReading;
            }
            if (hotWaterMeterReading) {
                document.getElementById('editHotWaterMeterReading').value = hotWaterMeterReading;
            }
            if (coldWaterMeterReading) {
                document.getElementById('editColdWaterMeterReading').value = coldWaterMeterReading;
            }
            document.getElementById('editUnit').value = unit;
            document.getElementById('editNotes').value = notes || '';

            // Prikaži modalni prozor
            const editModal = new bootstrap.Modal(document.getElementById('editConsumptionModal'));
            editModal.show();
        });
    });
}

// Funkcija za postavljanje event listenera za gumbe za uređivanje i brisanje mjernih mjesta
function setupMeasurePointButtons() {
    // Event listeneri za gumbe za uređivanje mjernih mjesta
    const editButtons = document.querySelectorAll('.edit-measure-point-btn');
    editButtons.forEach(button => {
        button.addEventListener('click', function() {
            const id = this.getAttribute('data-id');
            const name = this.getAttribute('data-name');
            const description = this.getAttribute('data-description');
            
            // Popuni formu za uređivanje
            document.getElementById('editMeasurePointId').value = id;
            document.getElementById('editMeasurePointName').value = name;
            document.getElementById('editMeasurePointDescription').value = description || '';
            
            // Postavi ID lokacije
            const locationId = document.getElementById('addMeasurePointLocationId').value;
            document.getElementById('editLocationId').value = locationId;
            
            // Prikaži formu za uređivanje
            document.getElementById('editMeasurePointForm').style.display = 'block';
        });
    });
    
    // Event listener za gumb za odustajanje od uređivanja
    const cancelEditBtn = document.getElementById('cancelEditBtn');
    if (cancelEditBtn) {
        cancelEditBtn.addEventListener('click', function() {
            // Sakrij formu za uređivanje
            document.getElementById('editMeasurePointForm').style.display = 'none';
        });
    }
    
    // Event listener za formu za uređivanje mjernih mjesta
    const editMeasurePointForm = document.getElementById('editMeasurePointFormElement');
    if (editMeasurePointForm) {
        editMeasurePointForm.addEventListener('submit', function(event) {
            event.preventDefault();
            
            const id = document.getElementById('editMeasurePointId').value;
            const locationId = document.getElementById('editLocationId').value;
            const name = document.getElementById('editMeasurePointName').value;
            const description = document.getElementById('editMeasurePointDescription').value;
            
            // Provjeri jesu li uneseni svi potrebni podaci
            if (!name) {
                alert('Molimo unesite naziv mjernog mjesta.');
                return;
            }
            
            // Pošalji zahtjev za uređivanje mjernog mjesta
            fetch('/api/measure-points/' + id, {
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({
                    name: name,
                    description: description,
                    location: {
                        id: locationId
                    }
                })
            })
            .then(response => {
                if (!response.ok) {
                    throw new Error('Greška pri uređivanju mjernog mjesta.');
                }
                return response.json();
            })
            .then(data => {
                // Sakrij formu za uređivanje
                document.getElementById('editMeasurePointForm').style.display = 'none';
                
                // Osvježi prikaz mjernih mjesta
                refreshMeasurePointsTable(locationId);
                
                // Automatski sakrij sve lokacije (klikni na "Sve lokacije" link)
                const allLocationsLink = document.querySelector('.list-group-item[href="#allLocations"]');
                if (allLocationsLink) {
                    allLocationsLink.click();
                }
            })
            .catch(error => {
                console.error('Error editing measure point:', error);
                alert('Greška pri uređivanju mjernog mjesta.');
            });
        });
    }
}

// Funkcija za učitavanje mjernih mjesta za odabranu lokaciju
function loadMeasurePoints(locationId, selectId, selectedMeasurePointId = null) {
    const measurePointSelect = document.getElementById(selectId);
    measurePointSelect.disabled = true;
    measurePointSelect.innerHTML = '<option value="">Učitavanje...</option>';

    fetch('/api/measure-points/' + locationId)
        .then(response => response.json())
        .then(data => {
            measurePointSelect.innerHTML = '<option value="">Odaberite mjerno mjesto</option>';
            data.forEach(measurePoint => {
                const option = document.createElement('option');
                option.value = measurePoint.id;
                option.textContent = measurePoint.name;
                if (selectedMeasurePointId && measurePoint.id == selectedMeasurePointId) {
                    option.selected = true;
                }
                measurePointSelect.appendChild(option);
            });
            measurePointSelect.disabled = false;
        })
        .catch(error => {
            console.error('Error loading measure points:', error);
            measurePointSelect.innerHTML = '<option value="">Greška pri učitavanju</option>';
            measurePointSelect.disabled = true;
        });
}

// Funkcija za osvježavanje prikaza mjernih mjesta
function refreshMeasurePointsTable(locationId) {
    fetch('/api/measure-points/' + locationId)
        .then(response => response.json())
        .then(data => {
            const tableBody = document.querySelector('#manageMeasurePointsModal table tbody');
            if (!tableBody) return;
            
            tableBody.innerHTML = '';
            
            if (data.length === 0) {
                const row = document.createElement('tr');
                row.innerHTML = '<td colspan="3" class="text-center">Nema mjernih mjesta za ovu lokaciju</td>';
                tableBody.appendChild(row);
            } else {
                data.forEach(measurePoint => {
                    const row = document.createElement('tr');
                    row.innerHTML = `
                        <td>${measurePoint.name}</td>
                        <td>${measurePoint.description || '-'}</td>
                        <td>
                            <button class="btn btn-sm btn-primary edit-measure-point-btn" 
                                    data-id="${measurePoint.id}" 
                                    data-name="${measurePoint.name}" 
                                    data-description="${measurePoint.description || ''}">
                                <i class="bi bi-pencil"></i> Uredi
                            </button>
                        </td>
                    `;
                    tableBody.appendChild(row);
                });
                
                // Dodaj event listenere za gumbe za uređivanje
                setupMeasurePointButtons();
            }
        })
        .catch(error => {
            console.error('Error loading measure points:', error);
            const tableBody = document.querySelector('#manageMeasurePointsModal table tbody');
            if (tableBody) {
                tableBody.innerHTML = '<tr><td colspan="3" class="text-center text-danger">Greška pri učitavanju mjernih mjesta.</td></tr>';
            }
        });
}

// Funkcija za inicijalizaciju animacije vode
function initWaterAnimation() {
    const waterAnimationContainer = document.getElementById('waterAnimation');
    if (!waterAnimationContainer) return;

    let sketch = function(p) {
        let particles = [];
        const numParticles = 100;
        
        p.setup = function() {
            p.createCanvas(waterAnimationContainer.offsetWidth, waterAnimationContainer.offsetHeight);
            for (let i = 0; i < numParticles; i++) {
                particles.push({
                    x: p.random(p.width),
                    y: p.random(p.height),
                    size: p.random(2, 5),
                    speedX: p.random(-0.5, 0.5),
                    speedY: p.random(-0.5, 0.5),
                    color: p.color(p.random(0, 100), p.random(100, 200), p.random(200, 255), p.random(150, 200))
                });
            }
        };
        
        p.draw = function() {
            p.background(255, 255, 255, 10);
            
            // Crtanje valova
            p.noFill();
            p.stroke(0, 119, 190, 50);
            p.strokeWeight(2);
            
            for (let j = 0; j < 3; j++) {
                p.beginShape();
                for (let i = 0; i <= p.width; i += 10) {
                    let y = p.sin(i * 0.01 + p.frameCount * 0.02 + j) * 10 + p.height / 2 + j * 20;
                    p.vertex(i, y);
                }
                p.endShape();
            }
            
            // Ažuriranje i crtanje čestica
            for (let i = 0; i < particles.length; i++) {
                let particle = particles[i];
                
                particle.x += particle.speedX;
                particle.y += particle.speedY;
                
                if (particle.x < 0) particle.x = p.width;
                if (particle.x > p.width) particle.x = 0;
                if (particle.y < 0) particle.y = p.height;
                if (particle.y > p.height) particle.y = 0;
                
                p.noStroke();
                p.fill(particle.color);
                p.ellipse(particle.x, particle.y, particle.size, particle.size);
            }
        };
        
        p.windowResized = function() {
            p.resizeCanvas(waterAnimationContainer.offsetWidth, waterAnimationContainer.offsetHeight);
        };
    };
    
    new p5(sketch, waterAnimationContainer);
}

// Funkcija za inicijalizaciju grafikona
function setupCharts() {
    setupConsumptionChart();
    setupComparisonChart();
}

// Funkcija za inicijalizaciju grafikona potrošnje
function setupConsumptionChart() {
    const consumptionChartCanvas = document.getElementById('consumptionChart');
    if (!consumptionChartCanvas) return;

    // Dohvati podatke iz data atributa
    const labels = JSON.parse(consumptionChartCanvas.getAttribute('data-labels'));
    const values = JSON.parse(consumptionChartCanvas.getAttribute('data-values'));
    
    new Chart(consumptionChartCanvas, {
        type: 'line',
        data: {
            labels: labels,
            datasets: [{
                label: 'Potrošnja vode (m³)',
                data: values,
                borderColor: '#0d6efd',
                backgroundColor: 'rgba(13, 110, 253, 0.1)',
                borderWidth: 2,
                tension: 0.3,
                fill: true
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: {
                    position: 'top',
                },
                tooltip: {
                    mode: 'index',
                    intersect: false,
                }
            },
            scales: {
                y: {
                    beginAtZero: true,
                    title: {
                        display: true,
                        text: 'Potrošnja (m³)'
                    }
                },
                x: {
                    title: {
                        display: true,
                        text: 'Datum'
                    }
                }
            }
        }
    });
}

// Funkcija za inicijalizaciju grafikona usporedbe
function setupComparisonChart() {
    const comparisonChartCanvas = document.getElementById('comparisonChart');
    if (!comparisonChartCanvas) return;

    // Dohvati podatke iz data atributa
    const labels = JSON.parse(comparisonChartCanvas.getAttribute('data-labels'));
    const userValues = JSON.parse(comparisonChartCanvas.getAttribute('data-user-values'));
    const avgValues = JSON.parse(comparisonChartCanvas.getAttribute('data-avg-values'));
    
    new Chart(comparisonChartCanvas, {
        type: 'bar',
        data: {
            labels: labels,
            datasets: [
                {
                    label: 'Vaša potrošnja',
                    data: userValues,
                    backgroundColor: 'rgba(13, 110, 253, 0.7)',
                    borderColor: '#0d6efd',
                    borderWidth: 1
                },
                {
                    label: 'Prosječna potrošnja',
                    data: avgValues,
                    backgroundColor: 'rgba(108, 117, 125, 0.7)',
                    borderColor: '#6c757d',
                    borderWidth: 1
                }
            ]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: {
                    position: 'top',
                },
                tooltip: {
                    mode: 'index',
                    intersect: false,
                }
            },
            scales: {
                y: {
                    beginAtZero: true,
                    title: {
                        display: true,
                        text: 'Potrošnja (m³)'
                    }
                },
                x: {
                    title: {
                        display: true,
                        text: 'Mjesec'
                    }
                }
            }
        }
    });
}

// Funkcija za postavljanje današnjeg datuma kao zadanog
function setDefaultDate() {
    // Set today's date as default for date inputs
    const today = new Date().toISOString().split('T')[0];
    const consumptionDateInput = document.getElementById('consumptionDate');
    if (consumptionDateInput) {
        consumptionDateInput.value = today;
    }
    
    const editConsumptionDate = document.getElementById('editConsumptionDate');
    if (editConsumptionDate) {
        editConsumptionDate.value = today;
    }
}

// Funkcija za inicijalizaciju modalnih prozora
function initModals() {
    // Inicijalizacija modalnog prozora za dodavanje potrošnje
    const addConsumptionModal = document.getElementById('addConsumptionModal');
    if (addConsumptionModal) {
        // Inicijalizacija Bootstrap modalnog prozora
        const bsAddConsumptionModal = new bootstrap.Modal(addConsumptionModal);
        
        // Kada se modalni prozor otvori, učitaj mjerna mjesta ako je lokacija već odabrana
        addConsumptionModal.addEventListener('shown.bs.modal', function() {
            const locationSelect = document.getElementById('locationSelect');
            if (locationSelect && locationSelect.value) {
                loadMeasurePoints(locationSelect.value, 'measurePointSelect');
            }
        });
        
        // Prikaži modalni prozor ako je zatraženo
        if (window.showAddConsumptionModal) {
            bsAddConsumptionModal.show();
        }
    }
    
    // Inicijalizacija modalnog prozora za upravljanje mjernim mjestima
    const manageMeasurePointsModal = document.getElementById('manageMeasurePointsModal');
    if (manageMeasurePointsModal) {
        // Inicijalizacija Bootstrap modalnog prozora
        const bsManageMeasurePointsModal = new bootstrap.Modal(manageMeasurePointsModal);
        
        // Kada se modalni prozor zatvori, automatski sakrij sva mjerna mjesta klikom na "Sve lokacije"
        manageMeasurePointsModal.addEventListener('hidden.bs.modal', function() {
            // Pronađi link "Sve lokacije" i klikni ga
            const allLocationsLinks = document.querySelectorAll('a.list-group-item');
            let allLocationsLink = null;
            
            // Pronađi link koji sadrži tekst "Sve lokacije"
            for (const link of allLocationsLinks) {
                if (link.textContent.includes('Sve lokacije')) {
                    allLocationsLink = link;
                    break;
                }
            }
            
            if (allLocationsLink) {
                allLocationsLink.click();
            } else {
                // Alternativni način - koristi URL
                window.location.href = '/dashboard';
            }
        });
        
        // Dodaj event listener za gumb "Spremi promjene" u formi za uređivanje mjernog mjesta
        const saveChangesButton = document.querySelector('#editMeasurePointForm button[type="submit"]');
        if (saveChangesButton) {
            saveChangesButton.addEventListener('click', function() {
                // Pronađi link "Sve lokacije" i klikni ga nakon što se forma pošalje
                setTimeout(() => {
                    const allLocationsLinks = document.querySelectorAll('a.list-group-item');
                    let allLocationsLink = null;
                    
                    // Pronađi link koji sadrži tekst "Sve lokacije"
                    for (const link of allLocationsLinks) {
                        if (link.textContent.includes('Sve lokacije')) {
                            allLocationsLink = link;
                            break;
                        }
                    }
                    
                    if (allLocationsLink) {
                        allLocationsLink.click();
                    } else {
                        // Alternativni način - koristi URL
                        window.location.href = '/dashboard';
                    }
                }, 500);
            });
        }
        
        // Prikaži modalni prozor ako je zatraženo
        if (window.showManageMeasurePointsModal) {
            bsManageMeasurePointsModal.show();
        }
    }
    
    // Inicijalizacija modalnog prozora za uređivanje potrošnje
    const editConsumptionModal = document.getElementById('editConsumptionModal');
    if (editConsumptionModal) {
        // Inicijalizacija Bootstrap modalnog prozora
        new bootstrap.Modal(editConsumptionModal);
    }
}
