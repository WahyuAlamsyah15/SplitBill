package com.splitBill.splitBill.service;

import java.util.List;
import java.util.UUID;

import com.splitBill.splitBill.model.User;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.splitBill.splitBill.dto.request.AddItemRequest;
import com.splitBill.splitBill.dto.request.UpdateItemRequest;
import com.splitBill.splitBill.dto.response.ItemResponse;
import com.splitBill.splitBill.handler.BadRequestException;
import com.splitBill.splitBill.handler.ResourceNotFoundException;
import com.splitBill.splitBill.model.Bill;
import com.splitBill.splitBill.model.BillItem;
import com.splitBill.splitBill.repository.BillItemRepository;
import com.splitBill.splitBill.repository.BillRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ItemService {

    private final BillItemRepository itemRepository;
    private final BillRepository billRepository;

    private String getCurrentTenantId() {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return user.getTenantDbName();
    }

    public ItemResponse toResponse(BillItem item){
        ItemResponse response = new ItemResponse();
        response.setId(item.getId());
        response.setName(item.getName());
        response.setPrice(item.getPrice());
        response.setQuantity(item.getQuantity());
        response.setSubtotal(item.getSubtotal());
        return response;
    }

    @Transactional
    public ItemResponse addItem(String billId, AddItemRequest request) {
        String tenantId = getCurrentTenantId();
        Bill bill = billRepository.findByIdAndTenantId(UUID.fromString(billId), tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Bill tidak ditemukan"));
        
        BillItem item = new BillItem();
        item.setBill(bill);
        item.setTenantId(tenantId);
        item.setName(request.getName());
        item.setPrice(request.getPrice());
        item.setQuantity(request.getQuantity());
        itemRepository.saveAndFlush(item);

        return toResponse(item);
    }

    public List<ItemResponse> getAll(String billId) {
        String tenantId = getCurrentTenantId();
        Bill bill = billRepository.findByIdAndTenantId(UUID.fromString(billId), tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Bill tidak ditemukan"));

        return bill.getItems().stream()
                .map(this::toResponse)
                .toList();
    }

    public ItemResponse getById(String billId, String id) {
        String tenantId = getCurrentTenantId();
        billRepository.findByIdAndTenantId(UUID.fromString(billId), tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Bill tidak ditemukan"));

        BillItem item = itemRepository.findById(UUID.fromString(id))
                .orElseThrow(() -> new ResourceNotFoundException("Item tidak ditemukan"));

        if (!item.getBill().getId().toString().equals(billId)) {
            throw new BadRequestException("Item tidak termasuk dalam Bill ini");
        }

        return toResponse(item);
    }

    @Transactional
    public ItemResponse edit(String billId, String id, UpdateItemRequest request){
        String tenantId = getCurrentTenantId();
        Bill bill = billRepository.findByIdAndTenantId(UUID.fromString(billId), tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Bill tidak ditemukan"));

        BillItem item = itemRepository.findById(UUID.fromString(id))
                .filter(i -> i.getBill().getId().equals(bill.getId()))
                .orElseThrow(() -> new ResourceNotFoundException("ID Item tidak ditemukan pada bill ini"));

        if (request.getName() != null) {
            if (request.getName().isBlank()) {
                throw new BadRequestException("Nama item tidak boleh kosong");
            }
            item.setName(request.getName());
        }
        if (request.getPrice() != null) {
            if (request.getPrice().signum() <= 0) {
                throw new BadRequestException("Harga item harus lebih dari nol");
            }
            item.setPrice(request.getPrice());
        }
        if (request.getQuantity() != null) {
            if (request.getQuantity() <= 0) {
                throw new BadRequestException("Kuantitas item harus lebih dari nol");
            }
            item.setQuantity(request.getQuantity());
        }

        itemRepository.saveAndFlush(item);
        return toResponse(item);
    }

    @Transactional
    public void delete(String billId, String id) {
        String tenantId = getCurrentTenantId();
        Bill bill = billRepository.findByIdAndTenantId(UUID.fromString(billId), tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Bill tidak ditemukan"));

        BillItem item = itemRepository.findById(UUID.fromString(id))
                .filter(i -> i.getBill().getId().equals(bill.getId()))
                .orElseThrow(() -> new ResourceNotFoundException("Item tidak ditemukan pada bill ini"));

        itemRepository.delete(item);
    }
}
