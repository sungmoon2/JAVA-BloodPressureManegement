// 공통 JavaScript 파일 생성
const BloodPressureManager = {
    // 삭제 진행 중 플래그
    isDeleting: false,

    // 모달 열기 함수
    openEditModal: function(element) {
        const id = element.getAttribute('data-id');
        const datetime = element.getAttribute('data-datetime');
        const systolic = element.getAttribute('data-systolic');
        const diastolic = element.getAttribute('data-diastolic');
        const pulse = element.getAttribute('data-pulse');
        const remark = element.getAttribute('data-remark');

        // 디버깅을 위한 데이터 출력
        console.log('Modal data:', {
            id, datetime, systolic, diastolic, pulse, remark
        });

        // 모달 필드에 값 설정
        document.getElementById('editId').value = id;
        document.getElementById('editDateTime').value = datetime;
        document.getElementById('editSystolic').value = systolic;
        document.getElementById('editDiastolic').value = diastolic;
        document.getElementById('editPulse').value = pulse;
        document.getElementById('editRemark').value = remark || '';

        // 모달 표시
        $('#editModal').modal('show');
    },

    // 저장 함수
    saveChanges: function() {
        try {
            const form = document.getElementById('editForm');

            // 입력값 검증
            if (!form.checkValidity()) {
                form.reportValidity();
                return;
            }

            const formData = {
                id: document.getElementById('editId').value,
                measureDateTime: document.getElementById('editDateTime').value,
                systolic: parseInt(document.getElementById('editSystolic').value),
                diastolic: parseInt(document.getElementById('editDiastolic').value),
                pulse: parseInt(document.getElementById('editPulse').value),
                remark: document.getElementById('editRemark').value || ''
            };

            // 디버깅을 위한 데이터 출력
            console.log('Sending data:', formData);

            // 유효성 검증
            if (!this.validateData(formData)) {
                return;
            }

            // CSRF 토큰 가져오기
            const token = document.querySelector("meta[name='_csrf']").content;
            const header = document.querySelector("meta[name='_csrf_header']").content;

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
                    location.reload();
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
        if (!data.id || !data.measureDateTime ||
            !data.systolic || !data.diastolic || !data.pulse) {
            alert('모든 필수 항목을 입력해주세요.');
            return false;
        }

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