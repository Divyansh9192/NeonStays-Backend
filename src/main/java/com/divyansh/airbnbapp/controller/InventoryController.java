package com.divyansh.airbnbapp.controller;

import com.divyansh.airbnbapp.dto.InventoryDTO;
import com.divyansh.airbnbapp.dto.UpdateInventoryRequestDTO;
import com.divyansh.airbnbapp.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    @GetMapping("/rooms/{roomId}")
    public ResponseEntity<List<InventoryDTO>> getAllInventoryByRoom(@PathVariable Long roomId){
        return ResponseEntity.ok(inventoryService.getAllInventoryByRoom(roomId));
    }

    @PatchMapping("rooms/{roomId}")
    public ResponseEntity<Void> updateInventory(@PathVariable Long roomId,
                                                @RequestBody UpdateInventoryRequestDTO updateInventoryRequestDTO) {
        inventoryService.updateInventory(roomId,updateInventoryRequestDTO);
        return ResponseEntity.noContent().build();

    }

}
