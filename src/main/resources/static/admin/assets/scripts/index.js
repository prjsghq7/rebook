const token = document.querySelector('meta[name="_csrf"]').getAttribute('content');
const header = document.querySelector('meta[name="_csrf_header"]').getAttribute('content');

const $userProvider = document.getElementById('userProvider');
const $userGender = document.getElementById('userGender');
const $userAgeGroup = document.getElementById('userAgeGroup');
const $dailyUserLogin = document.getElementById('dailyUserLogin');
const $dailyUserRegister = document.getElementById('dailyUserRegister');
const $dailyReviewRegister = document.getElementById('dailyReviewRegister');

const $reloadDailyUserLogin = document.getElementById('reloadDailyUserLogin');
const $reloadDailyUserRegister = document.getElementById('reloadDailyUserRegister');
const $reloadDailyReviewRegister = document.getElementById('reloadDailyReviewRegister');

const checkValidDate = (from, to) => {
    if (!from || !to) {
        dialog.showSimpleOk('통계 기간 설정', '기간을 모두 선택해주세요.');
        return false;
    }

    const fromDate = new Date(from);
    const toDate = new Date(to);
    const today = new Date();
    today.setHours(24, 0, 0, 0);

    if (fromDate > toDate) {
        dialog.showSimpleOk('통계 기간 설정', 'From(시작) 날짜는 To(종료) 날짜보다 이후 날짜 일 수 없습니다.');
        return false;
    }

    if (toDate > today) {
        dialog.showSimpleOk('통계 기간 설정', '현재 날짜 이후 날짜는 선택할 수 없습니다.');
        return false;
    }

    return true;
}

document.addEventListener('DOMContentLoaded', () => {
    const fromInputs = document.querySelectorAll('.date.from');
    const toInputs = document.querySelectorAll('.date.to');

    const today = new Date();
    const oneWeekAgo = new Date(today.getTime() - 7 * 24 * 60 * 60 * 1000);
    const fromValue = oneWeekAgo.toISOString().split('T')[0];
    const toValue = today.toISOString().split('T')[0];

    fromInputs.forEach(fromInput => {
        fromInput.value = fromValue;
    });
    toInputs.forEach(toInput => {
        toInput.value = toValue;
    });

    loadDashBoard(fromValue, toValue);
});

const updateUserProvider = (providerStats) => {
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

const updateUserAgeGroup = (ageGroupStats) => {
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

const updateDailyUserLogin = (dailyUserLoginStats) => {
    const dates = [];
    const counts = [];
    for (const stat of dailyUserLoginStats) {
        dates.push(stat.statDate);
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
            name: '접속한 회원 수',
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
            forceNiceScale: true,
            labels: {
                formatter: (val) => `${Math.floor(val)}명`
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
            formatter: (val) => `${val}명`,
            style: { fontSize: '12px' }
        }
    };

    $dailyUserLogin.innerHTML = '';
    const dailyUserChart = new ApexCharts($dailyUserLogin, options);
    dailyUserChart.render();
}

const updateDailyUserRegister = (dailyUserRegisterStats) => {
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

    $dailyUserRegister.innerHTML = '';
    const dailyUserChart = new ApexCharts($dailyUserRegister, options);
    dailyUserChart.render();
}

const updateDailyReviewRegister = (dailyReviewRegisterStats) => {
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

    $dailyReviewRegister.innerHTML = '';
    const dailyReviewChart = new ApexCharts($dailyReviewRegister, options);
    dailyReviewChart.render();
}

const loadDashBoard = (fromValue,  toValue) => {
    const xhr = new XMLHttpRequest();
    xhr.onreadystatechange = () => {
        if (xhr.readyState !== XMLHttpRequest.DONE) {
            return;
        }
        loading.hide();
        if (xhr.status < 200 || xhr.status >= 300) {
            dialog.showSimpleOk('관리자 통계', `[${xhr.status}]요청을 처리하는 도중 오류가 발생하였습니다.\n잠시 후 재시도 부탁드립니다.`);
            return;
        }
        const stats = JSON.parse(xhr.responseText);
        console.log(stats);

        updateUserProvider(stats.providerStats);
        updateUserGender(stats.genderStats);
        updateUserAgeGroup(stats.ageGroupStats);
        updateDailyUserLogin(stats.dailyUserLoginStats);
        updateDailyUserRegister(stats.dailyUserRegisterStats);
        updateDailyReviewRegister(stats.dailyReviewRegisterStats);
    };
    xhr.open('GET', `${origin}/admin/dashboard/all?from=${fromValue}&to=${toValue}`);
    xhr.send();
    loading.show('관리자 통계 불러오는 중');
};

document.getElementById('reloadUserProvider').addEventListener('click', () => {
    const xhr = new XMLHttpRequest();
    xhr.onreadystatechange = () => {
        if (xhr.readyState !== XMLHttpRequest.DONE) {
            return;
        }
        loading.hide();
        if (xhr.status < 200 || xhr.status >= 300) {
            dialog.showSimpleOk('소셜 로그인 비율 통계 갱신', `[${xhr.status}]요청을 처리하는 도중 오류가 발생하였습니다.\n잠시 후 재시도 부탁드립니다.`);
            return;
        }
        const providerStats = JSON.parse(xhr.responseText);
        updateUserProvider(providerStats);
    };
    xhr.open('GET', `${origin}/admin/dashboard/user-provider`);
    xhr.send();
    loading.show('소셜 로그인 비율 통계 갱신 중');
});

document.getElementById('reloadUserGender').addEventListener('click', () => {
    const xhr = new XMLHttpRequest();
    xhr.onreadystatechange = () => {
        if (xhr.readyState !== XMLHttpRequest.DONE) {
            return;
        }
        loading.hide();
        if (xhr.status < 200 || xhr.status >= 300) {
            dialog.showSimpleOk('회원 성별 비율 통계 갱신', `[${xhr.status}]요청을 처리하는 도중 오류가 발생하였습니다.\n잠시 후 재시도 부탁드립니다.`);
            return;
        }
        const genderStats = JSON.parse(xhr.responseText);
        updateUserGender(genderStats);
    };
    xhr.open('GET', `${origin}/admin/dashboard/user-gender`);
    xhr.send();
    loading.show('회원 성별 비율 통계 갱신 중');
});

document.getElementById('reloadUserAgeGroup').addEventListener('click', () => {
    const xhr = new XMLHttpRequest();
    xhr.onreadystatechange = () => {
        if (xhr.readyState !== XMLHttpRequest.DONE) {
            return;
        }
        loading.hide();
        if (xhr.status < 200 || xhr.status >= 300) {
            dialog.showSimpleOk('회원 연령 비율 통계 갱신', `[${xhr.status}]요청을 처리하는 도중 오류가 발생하였습니다.\n잠시 후 재시도 부탁드립니다.`);
            return;
        }
        const userGroupStats = JSON.parse(xhr.responseText);
        updateUserAgeGroup(userGroupStats);
    };
    xhr.open('GET', `${origin}/admin/dashboard/user-age-group`);
    xhr.send();
    loading.show('회원 연령 비율 통계 갱신 중');
});

$reloadDailyUserLogin.addEventListener('click', () => {
    const from = $reloadDailyUserLogin.parentElement.querySelector('.from').value;
    const to = $reloadDailyUserLogin.parentElement.querySelector('.to').value;
    if (!checkValidDate(from, to)) {
        return;
    }
    const xhr = new XMLHttpRequest();
    xhr.onreadystatechange = () => {
        if (xhr.readyState !== XMLHttpRequest.DONE) {
            return;
        }
        loading.hide();
        if (xhr.status < 200 || xhr.status >= 300) {
            dialog.showSimpleOk('일별 회원 접속 수 통계 갱신', `[${xhr.status}]요청을 처리하는 도중 오류가 발생하였습니다.\n잠시 후 재시도 부탁드립니다.`);
            return;
        }
        const dailyUserLoginStats = JSON.parse(xhr.responseText);
        updateDailyUserLogin(dailyUserLoginStats);
    };
    xhr.open('GET', `${origin}/admin/dashboard/daily-user-login?from=${from}&to=${to}`);
    xhr.send();
    loading.show('일별 회원 접속 수 통계 갱신 중');
});

$reloadDailyUserRegister.addEventListener('click', () => {
    const from = $reloadDailyUserRegister.parentElement.querySelector('.from').value;
    const to = $reloadDailyUserRegister.parentElement.querySelector('.to').value;
    if (!checkValidDate(from, to)) {
        return;
    }
    const xhr = new XMLHttpRequest();
    xhr.onreadystatechange = () => {
        if (xhr.readyState !== XMLHttpRequest.DONE) {
            return;
        }
        loading.hide();
        if (xhr.status < 200 || xhr.status >= 300) {
            dialog.showSimpleOk('일별 회원가입 수 통계 갱신', `[${xhr.status}]요청을 처리하는 도중 오류가 발생하였습니다.\n잠시 후 재시도 부탁드립니다.`);
            return;
        }
        const dailyUserRegisterStats = JSON.parse(xhr.responseText);
        updateDailyUserRegister(dailyUserRegisterStats);
    };
    xhr.open('GET', `${origin}/admin/dashboard/daily-user-register?from=${from}&to=${to}`);
    xhr.send();
    loading.show('일별 회원가입 수 통계 갱신 중');
});

$reloadDailyReviewRegister.addEventListener('click', () => {
    const from = $reloadDailyReviewRegister.parentElement.querySelector('.from').value;
    const to = $reloadDailyReviewRegister.parentElement.querySelector('.to').value;
    if (!checkValidDate(from, to)) {
        return;
    }
    const xhr = new XMLHttpRequest();
    xhr.onreadystatechange = () => {
        if (xhr.readyState !== XMLHttpRequest.DONE) {
            return;
        }
        loading.hide();
        if (xhr.status < 200 || xhr.status >= 300) {
            dialog.showSimpleOk('일별 리뷰 등록 수 통계 갱신', `[${xhr.status}]요청을 처리하는 도중 오류가 발생하였습니다.\n잠시 후 재시도 부탁드립니다.`);
            return;
        }
        const dailyReviewRegisterStats = JSON.parse(xhr.responseText);
        updateDailyReviewRegister(dailyReviewRegisterStats);
    };
    xhr.open('GET', `${origin}/admin/dashboard/daily-review-register?from=${from}&to=${to}`);
    xhr.send();
    loading.show('일별 리뷰 등록 수 통계 갱신 중');
});