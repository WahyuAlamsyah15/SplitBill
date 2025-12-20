package com.splitBill.splitBill.service;

import java.util.List;
import java.util.UUID;

import com.splitBill.splitBill.model.User;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.splitBill.splitBill.dto.request.AddParticipantRequest;
import com.splitBill.splitBill.dto.request.UpdateParticipantRequest;
import com.splitBill.splitBill.dto.response.ParticipantResponse;
import com.splitBill.splitBill.handler.BadRequestException;
import com.splitBill.splitBill.handler.ResourceNotFoundException;
import com.splitBill.splitBill.model.Bill;
import com.splitBill.splitBill.model.BillParticipant;
import com.splitBill.splitBill.repository.BillParticipantRepository;
import com.splitBill.splitBill.repository.BillRepository;
import com.splitBill.splitBill.repository.ItemAssignmentRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ParticipantService {

    private final BillParticipantRepository participantRepository;
    private final BillRepository billRepository;
    private final ItemAssignmentRepository assignmentRepository; // Inject ItemAssignmentRepository

    private String getCurrentTenantId() {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return user.getTenantDbName();
    }

    private ParticipantResponse mapToParticipantResponse(BillParticipant p) {
        ParticipantResponse res = new ParticipantResponse();
        res.setId(p.getId());
        res.setName(p.getName());
        return res;
    }

    @Transactional
    public ParticipantResponse addParticipant(String billId, AddParticipantRequest request) {
        String tenantId = getCurrentTenantId();
        Bill bill = billRepository.findByIdAndTenantId(UUID.fromString(billId), tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Bill tidak ditemukan"));

        BillParticipant participant = new BillParticipant();
        participant.setBill(bill);
        participant.setTenantId(tenantId);
        participant.setName(request.getName());
        participant = participantRepository.save(participant);
        return mapToParticipantResponse(participant);
    }

    @Transactional
    public ParticipantResponse edit(String billId, String id, UpdateParticipantRequest request){
        String tenantId = getCurrentTenantId();
        Bill bill = billRepository.findByIdAndTenantId(UUID.fromString(billId), tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Bill tidak ditemukan"));

        BillParticipant participant = participantRepository.findById(UUID.fromString(id))
                .filter(p -> p.getBill().getId().equals(bill.getId()))
                .orElseThrow(() -> new ResourceNotFoundException("ID participant tidak ditemukan pada bill ini"));

        if(request.getName() != null){
            participant.setName(request.getName());
        }
        participant = participantRepository.save(participant);
        return mapToParticipantResponse(participant);
    }

    public List<ParticipantResponse> getAllParticipants(String billId) {
        String tenantId = getCurrentTenantId();
        Bill bill = billRepository.findByIdAndTenantId(UUID.fromString(billId), tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Bill tidak ditemukan"));

        return bill.getParticipants().stream()
                .map(this::mapToParticipantResponse)
                .toList();
    }

    public ParticipantResponse getById(String billId, String id) {
        String tenantId = getCurrentTenantId();
        Bill bill = billRepository.findByIdAndTenantId(UUID.fromString(billId), tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Bill tidak ditemukan"));

        BillParticipant p = participantRepository.findById(UUID.fromString(id))
                .filter(participant -> participant.getBill().getId().equals(bill.getId()))
                .orElseThrow(() -> new ResourceNotFoundException("Participant tidak ditemukan pada bill ini"));

        return mapToParticipantResponse(p);
    }

    @Transactional
    public void delete(String billId, String id) {
        String tenantId = getCurrentTenantId();
        Bill bill = billRepository.findByIdAndTenantId(UUID.fromString(billId), tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Bill tidak ditemukan"));

        BillParticipant p = participantRepository.findById(UUID.fromString(id))
                .filter(participant -> participant.getBill().getId().equals(bill.getId()))
                .orElseThrow(() -> new ResourceNotFoundException("Participant tidak ditemukan pada bill ini"));

        assignmentRepository.deleteAllByParticipantId(p.getId()); // Delete associated item assignments
        participantRepository.delete(p);
    }
}
