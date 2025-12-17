package com.splitBill.splitBill.service;

import com.splitBill.splitBill.dto.response.RestoResponse;
import com.splitBill.splitBill.handler.BadRequestException;
import com.splitBill.splitBill.handler.ResourceNotFoundException;
import com.splitBill.splitBill.model.Resto;
import com.splitBill.splitBill.model.User;
import com.splitBill.splitBill.repository.RestoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RestoService {

    private final RestoRepository restoRepository;

    private String getCurrentTenantId() {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return user.getTenantDbName();
    }

    public List<RestoResponse> getAllResto() {
        String tenantId = getCurrentTenantId();
        return restoRepository.findByTenantId(tenantId).stream()
                .map(this::toRestoResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public RestoResponse createResto(String name) {
        String tenantId = getCurrentTenantId();

        if (restoRepository.existsByNameIgnoreCaseAndTenantId(name.trim(), tenantId)) {
            throw new BadRequestException("Resto dengan nama '" + name + "' sudah ada.");
        }

        Resto resto = new Resto();
        resto.setName(name.trim());
        resto.setTenantId(tenantId);
        Resto saved = restoRepository.save(resto);

        return toRestoResponse(saved);
    }

    @Transactional
    public RestoResponse updateResto(UUID id, String newName) {
        String tenantId = getCurrentTenantId();
        Resto resto = restoRepository.findById(id)
                .filter(r -> r.getTenantId().equals(tenantId))
                .orElseThrow(() -> new ResourceNotFoundException("Resto tidak ditemukan"));
        
        // Check if new name already exists in another resto for the same tenant
        if (restoRepository.existsByNameIgnoreCaseAndTenantId(newName.trim(), tenantId) && !resto.getName().equalsIgnoreCase(newName.trim())) {
            throw new BadRequestException("Resto dengan nama '" + newName + "' sudah ada.");
        }

        resto.setName(newName.trim());
        Resto updated = restoRepository.save(resto);

        return toRestoResponse(updated);
    }

    @Transactional
    public void softDeleteResto(UUID id) {
        String tenantId = getCurrentTenantId();
        Resto resto = restoRepository.findById(id)
                .filter(r -> r.getTenantId().equals(tenantId))
                .orElseThrow(() -> new ResourceNotFoundException("Resto tidak ditemukan"));
        
        // This will trigger the @SQLDelete annotation
        restoRepository.deleteById(resto.getId());
    }

    @Transactional
    public void deleteRestoWithAllBills(UUID id) {
        String tenantId = getCurrentTenantId();
        Resto resto = restoRepository.findById(id)
                .filter(r -> r.getTenantId().equals(tenantId))
                .orElseThrow(() -> new ResourceNotFoundException("Resto tidak ditemukan"));

        // This will bypass the soft-delete and perform a physical delete
        restoRepository.physicalDeleteById(resto.getId());
    }

    private RestoResponse toRestoResponse(Resto resto) {
        RestoResponse res = new RestoResponse();
        res.setId(resto.getId());
        res.setName(resto.getName());
        return res;
    }
}