document.addEventListener('DOMContentLoaded', function() {
    const infoBoxes = document.querySelectorAll('.info-box');
    const chartContainer = document.querySelector('.chart-container');
    const ctx = document.getElementById('dynamicChart').getContext('2d');
    let myChart;

    const chartsData = {
        cargo: {
            type: 'bar',
            data: {
                labels: ['Jan', 'Feb', 'Mär', 'Apr', 'Mai'],
                datasets: [{
                    label: 'Cargo',
                    data: [12, 19, 3, 5, 2, 3],
                    backgroundColor: 'rgba(35, 33, 32, 0.8)',
                }]
            },
            options: {
                scales: {
                    y: {
                        beginAtZero: true
                    },
                    x: {
                        ticks: {
                            font: {
                                weight: 'bold' // Macht die Beschriftung der X-Achse fett
                            }
                        }
                    }
                }
            }
        },
        // Weitere Datenobjekte für 'company', 'harbour', 'ship', 'map' können hier folgen
    };

    function handleBoxClick(data) {
        if (myChart) {
            myChart.destroy();
        }
        chartContainer.style.display = 'block';
        myChart = new Chart(ctx, chartsData[data]);
    }

    infoBoxes.forEach(box => {
        box.addEventListener('click', function() {
            handleBoxClick(this.id);
        });
    });
});
