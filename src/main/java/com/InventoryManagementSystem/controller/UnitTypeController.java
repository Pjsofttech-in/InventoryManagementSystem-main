package com.InventoryManagementSystem.controller;

import com.InventoryManagementSystem.entity.InventoryUnitType;
import com.InventoryManagementSystem.service.UnitTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@CrossOrigin(origins = "https://pjsofttech.com")
@RestController
public class UnitTypeController {

    @Autowired
    private UnitTypeService unitTypeService;

    @PostMapping("/createUnitType")
    public InventoryUnitType createUnitType(@RequestBody InventoryUnitType unitType)
    {
        return unitTypeService.saveUnitType(unitType);
    }

    @GetMapping("/getAllUnitTypes")
    public List<InventoryUnitType> getAllUnitTypes()
    {
        return unitTypeService.getAllUnitTypes();
    }

    @GetMapping("/getUnitTypeById/{id}")
    public InventoryUnitType getUnitTypeById(@PathVariable Long id)
    {
        return unitTypeService.getUnitTypeById(id);
    }

    @DeleteMapping("/deleteUnitType/{id}")
    public String deleteUnitType(@PathVariable Long id)
    {
        unitTypeService.deleteUnitType(id);
        return "UnitType ID " + id + " deleted successfully";
    }
}