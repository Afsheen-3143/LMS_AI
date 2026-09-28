package com.afsheen.aiassistant.lms.service.impl;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.afsheen.aiassistant.lms.entity.IdSequence;
import com.afsheen.aiassistant.lms.repository.IdSequenceRepository;

/**
 * Mints the next human-readable id (e.g. "S000003", "CRS004") for a given sequence name. MySQL has
 * no native SEQUENCE object, so this reads-increments-saves a row in id_sequence instead.
 */
@Component
public class IdSequenceGenerator {

    private final IdSequenceRepository idSequenceRepository;

    public IdSequenceGenerator(IdSequenceRepository idSequenceRepository) {
        this.idSequenceRepository = idSequenceRepository;
    }

    @Transactional
    public synchronized String next(String seqName, String prefix, int width) {
        IdSequence seq = idSequenceRepository.findById(seqName)
                .orElseThrow(() -> new IllegalStateException("Unknown id sequence: " + seqName));
        long value = seq.getNextVal();
        seq.setNextVal(value + 1);
        idSequenceRepository.save(seq);
        return prefix + String.format("%0" + width + "d", value);
    }
}
