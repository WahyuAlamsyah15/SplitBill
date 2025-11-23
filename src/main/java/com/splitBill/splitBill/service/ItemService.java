package com.splitBill.splitBill.service;

import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.splitBill.splitBill.dto.request.AddItemRequest;
import com.splitBill.splitBill.dto.request.UpdateItemRequest;
import com.splitBill.splitBill.dto.response.BillResponse;
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

    public ItemResponse toResponse(BillItem item){
        ItemResponse response = new ItemResponse();
        response.setId(item.getId());
        response.setName(item.getName());
        response.setPrice(item.getPrice());
        response.setQuantity(item.getQuantity());
        response.setSubtotal(item.getSubtotal());
        return response;
    }

    // CREATE
    @Transactional
    public ItemResponse addItem(String billId, AddItemRequest request) {
        Bill bill = billRepository.findById(UUID.fromString(billId))
                .orElseThrow(() -> new ResourceNotFoundException("Bill ID tidak ditemukan"));
        
        BillItem item = new BillItem();
        item.setBill(bill);
        item.setName(request.getName());
        item.setPrice(request.getPrice());
        item.setQuantity(request.getQuantity());
        itemRepository.saveAndFlush(item);

        return toResponse(item);
    }

    // READ - GET ALL
    public List<ItemResponse> getAll(String billId) {
        Bill bill = billRepository.findById(UUID.fromString(billId))
                .orElseThrow(() -> new ResourceNotFoundException("Bill tidak ditemukan"));

        return bill.getItems().stream()
                .map(this::toResponse)
                .toList();
    }

    // READ - GET BY ID
    public ItemResponse getById(String billId, String id) {
        BillItem item = itemRepository.findById(UUID.fromString(id))
                .orElseThrow(() -> new ResourceNotFoundException("Item tidak ditemukan"));

        if (!item.getBill().getId().toString().equals(billId)) {
            throw new BadRequestException("Item tidak termasuk Bill ini");
        }

        return toResponse(item);
    }

    // UPDATE
    @Transactional
    public ItemResponse edit(String billId, String id, UpdateItemRequest request){
        BillItem item = itemRepository.findById(UUID.fromString(id))
                .orElseThrow(() -> new ResourceNotFoundException("ID Item tidak ditemukan"));

        if (!item.getBill().getId().toString().equals(billId)) {
            throw new BadRequestException("Item tidak termasuk dalam bill ini");
        }

        if (request.getName() != null) item.setName(request.getName());
        if (request.getPrice() != null) item.setPrice(request.getPrice());
        if (request.getQuantity() != null) item.setQuantity(request.getQuantity());

        itemRepository.saveAndFlush(item);
        return toResponse(item);
    }

    // DELETE
    @Transactional
    public void delete(String billId, String id) {
        BillItem item = itemRepository.findById(UUID.fromString(id))
                .orElseThrow(() -> new ResourceNotFoundException("Item tidak ditemukan"));

        if (!item.getBill().getId().toString().equals(billId)) {
            throw new BadRequestException("Item tidak termasuk Bill ini");
        }

        itemRepository.delete(item);
    }
}
