package org.antagon.acore.util;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bukkit.Material;

public class MaterialValidator {
    public static Material validateMaterial(String materialName) {
        Material material = Material.matchMaterial(materialName);

        if (material == null) throw new IllegalArgumentException("Material not found");

        return material;
    }

    public static Set<Material> validateMaterials(List<String> checkList) {
        Set<Material> validMaterials = new HashSet<>();

        for (String materialName : checkList) {
            Material material = Material.matchMaterial(materialName);

            if (material != null) validMaterials.add(material);
        }

        return validMaterials;
    }

    public static Set<Material> validateMaterials(Set<String> checkList) {
        Set<Material> validMaterials = new HashSet<>();

        for (String materialName : checkList) {
            Material material = Material.matchMaterial(materialName);

            if (material != null) validMaterials.add(material);
        }

        return validMaterials;
    }
}