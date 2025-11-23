package com.splitBill.splitBill.service;

import java.util.List;
import java.util.UUID;

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

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ParticipantService {

    private final BillParticipantRepository participantRepository;
    private final BillRepository billRepository;

    private ParticipantResponse mapToParticipantResponse(BillParticipant p) {
        ParticipantResponse res = new ParticipantResponse();
        res.setId(p.getId());
        res.setName(p.getName());
        return res;
    }

    private ParticipantResponse createEntity(String billId, AddParticipantRequest request){
        BillParticipant participant = new BillParticipant();
        Bill bill = billRepository.findById(UUID.fromString(billId))
            .orElseThrow(() -> new ResourceNotFoundException("Id bill tidak ditemukan"));

        participant.setBill(bill);
        participant.setName(request.getName());
        participant = participantRepository.save(participant);
        return mapToParticipantResponse(participant);
    }

    public ParticipantResponse updateEntity(BillParticipant participant, UpdateParticipantRequest request){
        if(request.getName() != null){
            participant.setName(request.getName());
        }
        participant = participantRepository.save(participant);
        return mapToParticipantResponse(participant);
    }

    @Transactional
    public ParticipantResponse addParticipant(String billId, AddParticipantRequest request) {
        return createEntity(billId, request);
    }

    @Transactional
    public ParticipantResponse edit(String billId, String id, UpdateParticipantRequest request){
        BillParticipant participant = participantRepository.findById(UUID.fromString(id))
            .orElseThrow(() -> new ResourceNotFoundException("ID participant tidak ditemukan"));

        if (!participant.getBill().getId().toString().equals(billId)) {
            throw new BadRequestException("Item tidak termasuk dalam bill ini");
        }

        return updateEntity(participant, request);
    }

    // READ - GET ALL
    public List<ParticipantResponse> getAllParticipants(String billId) {
        Bill bill = billRepository.findById(UUID.fromString(billId))
            .orElseThrow(() -> new ResourceNotFoundException("Bill tidak ditemukan"));

        return bill.getParticipants().stream()
                .map(this::mapToParticipantResponse)
                .toList();
    }

    // READ - GET BY ID
    public ParticipantResponse getById(String id) {
        BillParticipant p = participantRepository.findById(UUID.fromString(id))
            .orElseThrow(() -> new ResourceNotFoundException("Participant tidak ditemukan"));

        return mapToParticipantResponse(p);
    }

    // DELETE
    @Transactional
    public void delete(String billId, String id) {
        BillParticipant p = participantRepository.findById(UUID.fromString(id))
            .orElseThrow(() -> new ResourceNotFoundException("Participant tidak ditemukan"));
        
        if (!p.getBill().getId().toString().equals(billId)) {
            throw new BadRequestException("Participants tidak termasuk Bill ini");
        }
        participantRepository.delete(p);
    }
}
