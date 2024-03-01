// Container für die Statistiken
const statistikContainer = document.getElementById('stats');

// Funktion zum Anzeigen der Daten als Balkendiagramme
function anzeigenBalkendiagramme(datensaetze) {
    for (const tabelle in datensaetze) {
        const daten = datensaetze[tabelle];
        anzeigenBalkendiagramm(daten, tabelle);
    }
}

// Funktion zum Erstellen eines Balkendiagramms
function anzeigenBalkendiagramm(daten, name) {
    const statistik = document.createElement('div');
    statistik.className = 'stat';
    statistik.innerHTML = `
        <h2>${name}</h2>
        <canvas id="${name}-chart"></canvas>
    `;
    statistikContainer.appendChild(statistik);

    const canvas = document.getElementById(`${name}-chart`);

    new Chart(canvas, {
        type: 'bar',
        data: {
            labels: Object.keys(daten),
            datasets: [{
                label: name,
                data: Object.values(daten),
                backgroundColor: 'rgba(255, 99, 132, 0.2)',
                borderColor: 'rgba(255, 99, 132, 1)',
                borderWidth: 1
            }]
        },
        options: {
            scales: {
                y: {
                    beginAtZero: true
                }
            }
        }
    });
}

// JSON-Daten abrufen und anzeigen
fetch('getData.php')
    .then(response => {
        if (!response.ok) {
            throw new Error('Network response was not ok');
        }
        return response.json();
    })
    .then(data => {
        anzeigenBalkendiagramme(data);
    })
    .catch(error => console.error('Error fetching or parsing data:', error));
