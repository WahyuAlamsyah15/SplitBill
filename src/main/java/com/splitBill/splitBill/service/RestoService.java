package com.splitBill.splitBill.service;

import com.splitBill.splitBill.dto.response.RestoResponse;
import com.splitBill.splitBill.handler.BadRequestException;
import com.splitBill.splitBill.handler.ResourceNotFoundException;
import com.splitBill.splitBill.model.Resto;
import com.splitBill.splitBill.repository.BillRepository;
import com.splitBill.splitBill.repository.RestoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RestoService {

    private final RestoRepository restoRepository;
    private final BillRepository billRepository;

    // GET ALL — LANGSUNG BALIKIN DTO!
    public List<RestoResponse> getAllResto() {
        return restoRepository.findAll().stream()
                .map(this::toRestoResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public RestoResponse createResto(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new BadRequestException("Nama resto tidak boleh kosong");
        }

        Resto resto = new Resto();
        resto.setName(name.trim());
        Resto saved = restoRepository.save(resto);

        return toRestoResponse(saved);
    }

    @Transactional
    public RestoResponse updateResto(UUID id, String newName) {
        Resto resto = restoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Resto tidak ditemukan"));

        if (newName == null || newName.trim().isEmpty()) {
            throw new BadRequestException("Nama resto tidak boleh kosong");
        }

        resto.setName(newName.trim());
        Resto updated = restoRepository.save(resto);

        return toRestoResponse(updated);
    }

    public String softDeleteResto(UUID id) {
        Resto resto = restoRepository.findByIdAndDeletedFalse(id)
            .orElseThrow(() -> new RuntimeException("Resto tidak ditemukan atau sudah dihapus"));

        resto.setDeleted(true);
        resto.setDeletedAt(LocalDateTime.now());
        restoRepository.save(resto);

        return "Resto \"" + resto.getName() + "\" berhasil diarsipkan! Bill tetap aman.";
    }


    public String deleteRestoWithAllBills(UUID id) {
        Resto resto = restoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Resto tidak ditemukan!"));

        long jumlahBill = billRepository.countByResto_Id(id);

        // Hapus resto → semua bill ikut kehapus karena cascade
        restoRepository.delete(resto);

        return String.format("Resto \"%s\" berhasil dihapus permanen! %d bill ikut terhapus.", 
                resto.getName(), jumlahBill);
    }

    // MAPPING DI SERVICE — SUPER CLEAN!
    private RestoResponse toRestoResponse(Resto resto) {
        RestoResponse res = new RestoResponse();
        res.setId(resto.getId());
        res.setName(resto.getName());
        return res;
    }
}