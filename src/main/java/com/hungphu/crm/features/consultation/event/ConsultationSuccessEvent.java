package com.hungphu.crm.features.consultation.event;

import com.hungphu.crm.features.consultation.entity.Consultation;
import lombok.Getter;

@Getter
public class ConsultationSuccessEvent {
    private final Consultation consultation;

    public ConsultationSuccessEvent(Consultation consultation) {
        this.consultation = consultation;
    }
}
