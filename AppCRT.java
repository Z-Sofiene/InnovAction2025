package com.zaghab_sofiene.project_iot.controller;

import com.zaghab_sofiene.project_iot.dao.DataEspDao;
import com.zaghab_sofiene.project_iot.model.DataEsp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@CrossOrigin("*")
@RestController
@RequestMapping("/api")
public class AppCRT {

    @Autowired
    private DataEspDao dao;

    private static Float latestBatteryLevel;
    private static Float latestDistance;
    private static Float latestAngelPanneauX;
    private static Float latestAngelPanneauY;
    private static Float latesthumiditeSol;

    @GetMapping("/data/liste")
    public List<DataEsp> getAllDataEsp() {
        return dao.findAll();
    }

    @PostMapping("/data/add")
    public ResponseEntity<Void> updateDataEsp(@RequestBody Map<String, Float> data) {

        float lvl = (float) (data.get("battery_lvl")*36.3/4095);

        latestDistance = data.get("distance");
        latestBatteryLevel = lvl;
        latestAngelPanneauX = data.get("angel_panneau_x");
        latestAngelPanneauY = data.get("angel_panneau_y");
        latesthumiditeSol = data.get("humidite_sol");
        
        DataEsp dataEsp = new DataEsp();
        dataEsp.setDistance(latestDistance);
        dataEsp.setBattery_lvl(latestBatteryLevel);
        dataEsp.setAngle_panneau_x(latestAngelPanneauX);
        dataEsp.setAngle_panneau_y(latestAngelPanneauY);
        dataEsp.setHumidite_sol(latesthumiditeSol);
        dao.save(dataEsp);
        
        return ResponseEntity.ok().build();
    }

    @GetMapping("/data/stream")
    public ResponseEntity<Map<String, Float>> getLatestDataEsp() {
        Map<String, Float> response = new HashMap<>();
        response.put("distance", latestDistance);
        response.put("battery_lvl", latestBatteryLevel);
        response.put("angel_panneau_x", latestAngelPanneauX);
        response.put("angel_panneau_Y", latestAngelPanneauX);
        response.put("humidite_sol", latesthumiditeSol);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}

