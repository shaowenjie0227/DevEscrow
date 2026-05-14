package com.programmer.escrow.demand.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class DemandPayload {

    private List<DemandFileItem> images = new ArrayList<>();

    private List<DemandFileItem> attachments = new ArrayList<>();

    private List<DemandStagePlan> stagePlans = new ArrayList<>();
}
