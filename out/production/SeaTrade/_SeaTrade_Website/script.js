document.addEventListener('DOMContentLoaded', function() {
    const infoBoxes = document.querySelectorAll('.info-box');
    const chartContainer = document.querySelector('.chart-container');
    const ctx = document.getElementById('dynamicChart').getContext('2d');
    let myChart;

    const chartsData = {
        cargo: {
            type: 'bar',
            data: {
                labels: [], // Empty labels array to be filled with CargoIDs
                datasets: [{
                    label: 'Cargo Value',
                    data: [], // Empty data array to be filled with cargo values
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
		company: {
            type: 'bar',
            data: {
                labels: [], // Empty labels array to be filled with Company names
                datasets: [{
                    label: 'Company Balance',
                    data: [], // Empty data array to be filled with company balances
                    backgroundColor: 'rgba(65, 105, 225, 0.8)', // Adjust the color as needed
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
                                weight: 'bold'
                            }
                        }
                    }
                }
            }
        },
		 harbour: {
            type: 'pie',
            data: {
                labels: [], // Empty labels array to be filled with Harbour names
                datasets: [{
                    data: [], // Empty data array to be filled with the number of cargos for each harbour
                    backgroundColor: [], // Colors for the pie chart slices
                }]
            },
            options: {
                responsive: true,
				aspectRatio: 2,
            }
        },
        // Weitere Datenobjekte für 'company', 'harbour', 'ship', 'map' können hier folgen
    };

	function createChart(data) {
        if (myChart) {
            myChart.destroy();
        }
        chartContainer.style.display = 'block';
        myChart = new Chart(document.getElementById('dynamicChart').getContext('2d'), data);
    }
	
    function handleBoxClick(data) {
        fetch('getData.php')
            .then(response => response.json())
            .then(responseData => {
                // get data
                const cargoData = responseData.cargoData;
				const companyData = responseData.companyData;
				const harbourData = responseData.harbourData;
				
                // fill chart
                chartsData.cargo.data.labels = cargoData.map(item => item.CargoID);
                chartsData.cargo.data.datasets[0].data = cargoData.map(item => item.Value);
				
				chartsData.company.data.labels = companyData.map(item => item.Name);
                chartsData.company.data.datasets[0].data = companyData.map(item => item.Balance);
				
				chartsData.harbour.data.labels = harbourData.map(item => item.Name);
                chartsData.harbour.data.datasets[0].data = harbourData.map(item => {
                    const cargoCount = responseData.cargoData.filter(cargo => cargo.HarbourIDStart === item.HarbourID).length;
                    return cargoCount;
                });

                // Generate random colors for each pie chart slice
                chartsData.harbour.data.datasets[0].backgroundColor = Array.from({ length: harbourData.length }, () => getRandomColor());

                createChart(chartsData[data]);
            })
            .catch(error => {
                console.error('Error fetching data:', error);
            });
    }

	function getRandomColor() {
        const letters = '0123456789ABCDEF';
        let color = '#';
        for (let i = 0; i < 6; i++) {
            color += letters[Math.floor(Math.random() * 16)];
        }
        return color;
    }
	
    infoBoxes.forEach(box => {
        box.addEventListener('click', function() {
            handleBoxClick(this.id);
        });
    });
});