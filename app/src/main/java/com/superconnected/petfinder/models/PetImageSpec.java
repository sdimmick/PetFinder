package com.superconnected.petfinder.models;

import com.yahoo.squidb.annotations.ColumnSpec;
import com.yahoo.squidb.annotations.TableModelSpec;

@TableModelSpec(className = "PetImage", tableName = "pet_images")
public class PetImageSpec {
    @ColumnSpec(constraints = "UNIQUE ON CONFLICT IGNORE")
    String path;
    String classification;
    String classificationId;
    float classificationScore;
}
