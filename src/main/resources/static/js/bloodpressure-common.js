// 공통 JavaScript 파일 생성
const BloodPressureManager = {
    // 삭제 진행 중 플래그
    isDeleting: false,

    // 모달 열기 함수
    openEditModal: function(element) {
        // 데이터 속성 읽기 전에 요소 확인
        if (!element) {
            console.error('Element is null');
            return;
        }

        // 데이터 읽기 및 로깅
        const id = element.getAttribute('data-id');
        const datetime = element.getAttribute('data-datetime');
        const systolic = element.getAttribute('data-systolic');
        const diastolic = element.getAttribute('data-diastolic');
        const pulse = element.getAttribute('data-pulse');
        const remark = element.getAttribute('data-remark');

        console.log('Opening modal with data:', {
            id, datetime, systolic, diastolic, pulse, remark
        });

        // datetime 형식 변환
        const formattedDatetime = datetime ? datetime.substring(0, 16) : '';
        console.log('Formatted datetime:', formattedDatetime);

        // 모달 필드 설정 전에 요소 존재 확인
        const editId = document.getElementById('editId');
        const editDateTime = document.getElementById('editDateTime');
        const editSystolic = document.getElementById('editSystolic');
        const editDiastolic = document.getElementById('editDiastolic');
        const editPulse = document.getElementById('editPulse');
        const editRemark = document.getElementById('editRemark');

        if (!editId || !editDateTime || !editSystolic || !editDiastolic || !editPulse) {
            console.error('Required form fields not found');
            alert('필수 입력 필드를 찾을 수 없습니다.');
            return;
        }

        // 값 설정
        editId.value = id;
        editDateTime.value = formattedDatetime;
        editSystolic.value = systolic;
        editDiastolic.value = diastolic;
        editPulse.value = pulse;
        if (editRemark) editRemark.value = remark || '';

        // 설정된 값 확인
        console.log('Set form values:', {
            id: editId.value,
            datetime: editDateTime.value,
            systolic: editSystolic.value,
            diastolic: editDiastolic.value,
            pulse: editPulse.value,
            remark: editRemark?.value
        });

        // 모달 표시
        $('#editModal').modal('show');
    },

    // 저장 함수
    saveChanges: function() {
        try {
            const form = document.getElementById('editForm');
            if (!form) {
                throw new Error('편집 폼을 찾을 수 없습니다.');
            }

            // 입력값 검증
            if (!form.checkValidity()) {
                form.reportValidity();
                return;
            }

            const editId = document.getElementById('editId');
            const editDateTime = document.getElementById('editDateTime');
            const editSystolic = document.getElementById('editSystolic');
            const editDiastolic = document.getElementById('editDiastolic');
            const editPulse = document.getElementById('editPulse');
            const editRemark = document.getElementById('editRemark');

            if (!editId || !editDateTime || !editSystolic || !editDiastolic || !editPulse) {
                throw new Error('필수 입력 필드를 찾을 수 없습니다.');
            }

            const formData = {
                id: editId.value,
                measureDatetime: editDateTime.value,
                systolic: parseInt(editSystolic.value, 10),
                diastolic: parseInt(editDiastolic.value, 10),
                pulse: parseInt(editPulse.value, 10),
                remark: editRemark ? editRemark.value : ''
            };

            // 유효성 검증
            if (!this.validateData(formData)) {
                return;
            }

            // CSRF 토큰 가져오기
            const token = document.querySelector("meta[name='_csrf']").content;
            const header = document.querySelector("meta[name='_csrf_header']").content;

            // 현재 페이지 경로 확인
            const currentPath = window.location.pathname;
            const isChartPage = currentPath.includes('/chart');

            // 현재 활성 탭 ID 저장 (차트 페이지인 경우에만)
            let activeTabId = null;
            if (isChartPage) {
                const activeTab = document.querySelector('.nav-link.active');
                if (activeTab) {
                    activeTabId = activeTab.getAttribute('id');
                }
            }

            fetch('/bloodpressure/update', {
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json',
                    [header]: token
                },
                body: JSON.stringify(formData)
            })
                .then(response => {
                    if (!response.ok) {
                        return response.json().then(err => {
                            throw new Error(err.error || '수정에 실패했습니다.');
                        });
                    }
                    return response.json();
                })
                .then(data => {
                    alert('수정이 완료되었습니다.');
                    $('#editModal').modal('hide');

                    // 페이지별 처리
                    if (isChartPage) {
                        // 차트 페이지인 경우
                        const tabMapping = {
                            'tab-24h': '#hours24',
                            'tab-7d': '#days7',
                            'tab-3m': '#months3',
                            'tab-6m': '#months6'
                        };

                        const hash = activeTabId ? tabMapping[activeTabId] : '#hours24';
                        const newUrl = `/bloodpressure/chart${hash}`;

                        // 현재 URL과 새로운 URL이 같은 경우에도 강제로 새로고침
                        window.location.href = newUrl;
                        window.location.reload(true);
                    } else {
                        // 데이터 페이지인 경우
                        window.location.href = '/bloodpressure/data';
                        window.location.reload(true);
                    }
                })
                .catch(error => {
                    console.error('Error:', error);
                    alert('수정 실패: ' + error.message);
                });
        } catch (error) {
            console.error('Error in saveChanges:', error);
            alert('저장 중 오류가 발생했습니다: ' + error.message);
        }
    },
    // 삭제 함수
    deleteRecord: function(id) {
        // confirm 중복 호출 방지
        if (this.isDeleting) return;
        this.isDeleting = true;

        if (!confirm('정말로 이 데이터를 삭제하시겠습니까?')) {
            this.isDeleting = false;
            return;
        }

        // CSRF 토큰 가져오기
        const token = document.querySelector("meta[name='_csrf']").content;
        const header = document.querySelector("meta[name='_csrf_header']").content;

        console.log('Deleting record:', id);
        console.log('CSRF Token:', token);
        console.log('CSRF Header:', header);

        fetch(`/bloodpressure/delete/${id}`, {
            method: 'DELETE',
            headers: {
                'Content-Type': 'application/json',
                [header]: token
            }
        })
            .then(response => {
                if (!response.ok) {
                    throw new Error('삭제에 실패했습니다.');
                }
                console.log('Delete successful');
                alert('삭제가 완료되었습니다.');
                location.reload();
            })
            .catch(error => {
                console.error('Delete error:', error);
                alert('삭제 실패: ' + error.message);
            })
            .finally(() => {
                this.isDeleting = false;
            });
    },

    // 데이터 유효성 검증
    validateData: function(data) {
        console.log('Validating data:', data); // 디버깅을 위한 로그 추가

        // null 체크와 형식 검증을 분리
        if (!data.id) {
            console.error('ID is missing');
            alert('데이터 ID가 없습니다.');
            return false;
        }

        if (!data.measureDatetime) {
            console.error('Datetime is missing');
            alert('측정 날짜/시간을 입력해주세요.');
            return false;
        }

        // 숫자 데이터 검증 시 0도 유효한 값으로 처리
        if (data.systolic === undefined || data.systolic === null || isNaN(data.systolic)) {
            console.error('Invalid systolic:', data.systolic);
            alert('수축기 혈압을 올바르게 입력해주세요.');
            return false;
        }

        if (data.diastolic === undefined || data.diastolic === null || isNaN(data.diastolic)) {
            console.error('Invalid diastolic:', data.diastolic);
            alert('이완기 혈압을 올바르게 입력해주세요.');
            return false;
        }

        if (data.pulse === undefined || data.pulse === null || isNaN(data.pulse)) {
            console.error('Invalid pulse:', data.pulse);
            alert('맥박을 올바르게 입력해주세요.');
            return false;
        }

        // 값 범위 검증
        if (data.systolic < 0 || data.systolic > 300) {
            alert('수축기 혈압은 0-300 사이의 값이어야 합니다.');
            return false;
        }

        if (data.diastolic < 0 || data.diastolic > 200) {
            alert('이완기 혈압은 0-200 사이의 값이어야 합니다.');
            return false;
        }

        if (data.pulse < 0 || data.pulse > 200) {
            alert('맥박은 0-200 사이의 값이어야 합니다.');
            return false;
        }

        return true;
    }

    };

// 숫자 입력 필드의 유효성 검사 이벤트 리스너
document.addEventListener('DOMContentLoaded', function() {
    const editSystolic = document.getElementById('editSystolic');
    const editDiastolic = document.getElementById('editDiastolic');
    const editPulse = document.getElementById('editPulse');

    if (editSystolic) {
        editSystolic.addEventListener('input', function() {
            if (this.value < 0) this.value = 0;
            if (this.value > 300) this.value = 300;
        });
    }

    if (editDiastolic) {
        editDiastolic.addEventListener('input', function() {
            if (this.value < 0) this.value = 0;
            if (this.value > 200) this.value = 200;
        });
    }

    if (editPulse) {
        editPulse.addEventListener('input', function() {
            if (this.value < 0) this.value = 0;
            if (this.value > 200) this.value = 200;
        });
    }

    // ESC 키로 모달 닫기
    document.addEventListener('keydown', function(e) {
        if (e.key === 'Escape') {
            $('#editModal').modal('hide');
        }
    });

    // 모달 외부 클릭으로 닫기
    const editModal = document.getElementById('editModal');
    if (editModal) {
        editModal.addEventListener('click', function(e) {
            if (e.target === this) {
                $('#editModal').modal('hide');
            }
        });
    }
});