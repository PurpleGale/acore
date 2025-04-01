package org.antagon.acore.util;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.bukkit.Material;

public class MaterialValidator {
    public static Material validateMaterial(String materialName) {
        Material material = Material.matchMaterial(materialName);

        if (material == null) throw new IllegalArgumentException("Material not found");

        return material;
    }

    public static Set<Material> validateMaterials(Collection<String> checkList) {
        Set<Material> validMaterials = new HashSet<>();

        for (String materialName : checkList) {
            Material material = Material.matchMaterial(materialName);

            if (material != null) validMaterials.add(material);
        }

        return validMaterials;
    }
}