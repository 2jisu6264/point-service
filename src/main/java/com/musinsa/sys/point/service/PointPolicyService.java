package com.musinsa.sys.point.service;

import com.musinsa.sys.point.repository.PointPolicyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.service.spi.ServiceException;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PointPolicyService {
    private final PointPolicyRepository policyRepository;

    public long getValue(String key) {
        return policyRepository.findByPolicyKey(key)
                .orElseThrow(() -> new ServiceException("HCO999"))
                .getPolicyValue();
    }

}
