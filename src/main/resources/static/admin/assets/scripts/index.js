const token = document.querySelector('meta[name="_csrf"]').getAttribute('content');
const header = document.querySelector('meta[name="_csrf_header"]').getAttribute('content');

const $userProvider = document.getElementById('userProvider');
const $userGender = document.getElementById('userGender');
const $userAgeGroup = document.getElementById('userAgeGroup');
const $userRegister = document.getElementById('userRegister');
const $reviewRegister = document.getElementById('reviewRegister');

const updateUserProvider = (providerStats) => {
    console.log(providerStats);

    const labels = [];
    const series = [];
    for (const stat of providerStats) {
        labels.push(stat.provider);
        series.push(stat.userCount);
    }

    const chartOption = {
        labels: labels,
        series: series,
        chart: {
            type: 'pie',
            width: 300,
            height: 330,
            toolbar: {
                autoSelected: 'pan',
                show: false
            },
            zoom: {
                enabled: false
            }
        },
        legend: {
            position: 'bottom'
        }
    };
    $userProvider.innerHTML = '';
    const providerChart = new ApexCharts($userProvider, chartOption);
    providerChart.render();
}

const updateUserGender = (genderStats) => {
    console.log(genderStats);

    const labels = [];
    const series = [];
    for (const stat of genderStats) {
        labels.push(stat.gender);
        series.push(stat.userCount);
    }

    const chartOption = {
        labels: labels,
        series: series,
        chart: {
            type: 'pie',
            width: 300,
            height: 330,
            toolbar: {
                autoSelected: 'pan',
                show: false
            },
            zoom: {
                enabled: false
            }
        },
        legend: {
            position: 'bottom'
        }
    };
    $userGender.innerHTML = '';
    const genderChart = new ApexCharts($userGender, chartOption);
    genderChart.render()
}

const updateAgeGroup = (ageGroupStats) => {
    console.log(ageGroupStats);

    const labels = [];
    const series = [];
    for (const stat of ageGroupStats) {
        labels.push(stat.ageGroup);
        series.push(stat.userCount);
    }

    const chartOption = {
        labels: labels,
        series: series,
        chart: {
            type: 'pie',
            width: 300,
            height: 330,
            toolbar: {
                autoSelected: 'pan',
                show: false
            },
            zoom: {
                enabled: false
            }
        },
        legend: {
            position: 'bottom'
        }
    };

    $userAgeGroup.innerHTML = '';
    const ageGroupChart = new ApexCharts($userAgeGroup, chartOption);
    ageGroupChart.render();
}

const updateUserRegister = (dailyUserRegisterStats) => {
    console.log(dailyUserRegisterStats);

    const dates = [];
    const counts = [];
    for (const stat of dailyUserRegisterStats) {
        dates.push(stat.date);
        counts.push(stat.userCount);
    }

    const options = {
        chart: {
            type: 'line',
            height: 350,
            toolbar: {
                show: true
            },
            zoom: {
                enabled: true
            }
        },
        series: [{
            name: '가입자 수',
            data: counts
        }],
        xaxis: {
            categories: dates,
            title: {
                text: '날짜',
                style: { fontSize: '14px', fontWeight: 'bold' }
            }
        },
        yaxis: {
            title: {
                text: '회원 수',
                style: { fontSize: '14px', fontWeight: 'bold' }
            },
            min: 0,
            forceNiceScale: true,  // 자연스러운 스케일
            labels: {
                formatter: (val) => `${Math.floor(val)}명` // 정수로
            }
        },
        stroke: {
            curve: 'smooth', // 부드러운 곡선
            width: 3
        },
        markers: {
            size: 4,
            colors: ['#008FFB'],
            strokeColors: '#fff',
            strokeWidth: 2
        },
        colors: ['#268dfa'], // 선 색상
        dataLabels: {
            enabled: true,
            offsetX: 10,
            formatter: (val) => `${val}명`,
            style: { fontSize: '12px' }
        }
    };

    $userRegister.innerHTML = '';
    const dailyUserChart = new ApexCharts($userRegister, options);
    dailyUserChart.render();
}

const updateReviewRegister = (dailyReviewRegisterStats) => {
    console.log(dailyReviewRegisterStats);

    const dates = [];
    const counts = [];
    for (const stat of dailyReviewRegisterStats) {
        dates.push(stat.date);
        counts.push(stat.reviewCount);
    }

    const options = {
        chart: {
            type: 'line',
            height: 350,
            toolbar: {
                show: true
            },
            zoom: {
                enabled: true
            }
        },
        series: [{
            name: '리뷰 수',
            data: counts
        }],
        xaxis: {
            categories: dates,
            title: {
                text: '날짜',
                style: { fontSize: '14px', fontWeight: 'bold' }
            }
        },
        yaxis: {
            title: {
                text: '리뷰 수',
                style: { fontSize: '14px', fontWeight: 'bold' }
            },
            min: 0,
            forceNiceScale: true,
            labels: {
                formatter: (val) => `${Math.floor(val)}개`
            }
        },
        stroke: {
            curve: 'smooth',
            width: 3
        },
        markers: {
            size: 4,
            colors: ['#008FFB'],
            strokeColors: '#fff',
            strokeWidth: 2
        },
        colors: ['#268dfa'],
        dataLabels: {
            enabled: true,
            offsetX: 10,
            formatter: (val) => `${val}개`,
            style: { fontSize: '12px' }
        }
    };

    $reviewRegister.innerHTML = '';
    const dailyReviewChart = new ApexCharts($reviewRegister, options);
    dailyReviewChart.render();
}

const loadDashBoard = () => {
    const xhr = new XMLHttpRequest();
    const formData = new FormData();
    
    xhr.onreadystatechange = () => {
        if (xhr.readyState !== XMLHttpRequest.DONE) {
            return;
        }
        if (xhr.status < 200 || xhr.status >= 300) {
            dialog.showSimpleOk('관리자 통계', `[${xhr.status}]요청을 처리하는 도중 오류가 발생하였습니다.\n잠시 후 재시도 부탁드립니다.`);
            return;
        }
        const stats = JSON.parse(xhr.responseText);
        console.log(stats);

        updateUserProvider(stats.providerStats);
        updateUserGender(stats.genderStats);
        updateAgeGroup(stats.ageGroupStats);
        updateUserRegister(stats.dailyUserRegisterStats);
        updateReviewRegister(stats.dailyReviewRegisterStats);
    };
    xhr.open('GET', '/admin/dashboard/all');
    xhr.send(formData);
};

loadDashBoard();