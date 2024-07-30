package com.example.oauth2server.global.service;

import com.example.oauth2server.global.entity.Client;
import com.example.oauth2server.global.repository.ClientRepository;
import com.example.oauth2server.global.util.ClientUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.stereotype.Service;

/**
 * oAuth2 서버를 구성하기 위한 필수 컴포넌트로
 * SecurotyConfig 클래스의 InMemoryRegisteredClientRepository는 테스트 시점에 사용하고
 * RegisteredClientRepository를 통해서 실제 DB에 Client 정보를 등록/조회해야 함
 */
@Service
@RequiredArgsConstructor
public class CustomRegisteredClientRepository implements RegisteredClientRepository {

    private final ClientRepository clientRepository;
    private final ClientUtils clientUtils;

    @Override
    public void save(RegisteredClient registeredClient) {
        Client client = clientUtils.toEntity(registeredClient);
        clientRepository.save(client);
    }

    @Override
    public RegisteredClient findById(String id) {
        Client client = clientRepository.findById(id).orElseThrow();
        return clientUtils.toObject(client);
    }

    @Override
    public RegisteredClient findByClientId(String clientId) {
        Client client = clientRepository.findByClientId(clientId).orElseThrow();
        return clientUtils.toObject(client);
    }
}
