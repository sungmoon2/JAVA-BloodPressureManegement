package com.example.bpmanagement.DTO;

import java.time.LocalDateTime;

public class BloodPressureDTO {

    // 측정 날짜와 시간
    private LocalDateTime measureDatetime;
    // 수축기 혈압 (Systolic blood pressure)
    private int systolic;
    // 이완기 혈압 (Diastolic blood pressure)
    private int diastolic;
    // 맥박 (Pulse)
    private int pulse;
    // 비고 (Additional remarks or notes)
    private String remark;

    /**
     * 혈압 측정 날짜와 시간을 가져오는 메서드입니다.
     *
     * @return measureDatetime 측정 날짜와 시간
     */
    public LocalDateTime getMeasureDatetime() {
        return measureDatetime;
    }

    /**
     * 혈압 측정 날짜와 시간을 설정하는 메서드입니다.
     *
     * @param measureDatetime 측정 날짜와 시간
     */
    public void setMeasureDatetime(LocalDateTime measureDatetime) {
        this.measureDatetime = measureDatetime;
    }

    /**
     * 수축기 혈압 (Systolic)을 가져오는 메서드입니다.
     *
     * @return systolic 수축기 혈압
     */
    public int getSystolic() {
        return systolic;
    }

    /**
     * 수축기 혈압 (Systolic)을 설정하는 메서드입니다.
     *
     * @param systolic 수축기 혈압
     */
    public void setSystolic(int systolic) {
        this.systolic = systolic;
    }

    /**
     * 이완기 혈압 (Diastolic)을 가져오는 메서드입니다.
     *
     * @return diastolic 이완기 혈압
     */
    public int getDiastolic() {
        return diastolic;
    }

    /**
     * 이완기 혈압 (Diastolic)을 설정하는 메서드입니다.
     *
     * @param diastolic 이완기 혈압
     */
    public void setDiastolic(int diastolic) {
        this.diastolic = diastolic;
    }

    /**
     * 맥박 (Pulse)을 가져오는 메서드입니다.
     *
     * @return pulse 맥박
     */
    public int getPulse() {
        return pulse;
    }

    /**
     * 맥박 (Pulse)을 설정하는 메서드입니다.
     *
     * @param pulse 맥박
     */
    public void setPulse(int pulse) {
        this.pulse = pulse;
    }

    /**
     * 비고 (Additional remarks or notes)를 가져오는 메서드입니다.
     *
     * @return remark 비고
     */
    public String getRemark() {
        return remark;
    }

    /**
     * 비고 (Additional remarks or notes)를 설정하는 메서드입니다.
     *
     * @param remark 비고
     */
    public void setRemark(String remark) {
        this.remark = remark;
    }
}
