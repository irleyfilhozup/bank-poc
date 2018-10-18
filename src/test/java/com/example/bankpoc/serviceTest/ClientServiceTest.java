package com.example.bankpoc.serviceTest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collection;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import com.example.bankpoc.base.BankBaseTest;
import com.example.bankpoc.exception.BusinessException;
import com.example.bankpoc.exception.NonExistentException;
import com.example.bankpoc.models.entity.Account;
import com.example.bankpoc.models.entity.Client;
import com.example.bankpoc.models.request.ClientRequest;
import com.example.bankpoc.models.response.ClientResponse;
import com.example.bankpoc.repository.ClientRepository;
import com.example.bankpoc.service.implement.ClientServiceImpl;
import com.example.bankpoc.service.interfaceServ.AccountService;

public class ClientServiceTest extends BankBaseTest {

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private AccountService accountService;

    @InjectMocks
    private ClientServiceImpl clientService;

    private Client client1;
    private Client client2;
    private Account account1;
    private ClientRequest clientRequest;
    private Collection<Client> allClients;
    private int id;

    @Before
    public void setUp() {
        client1 = new Client("Joao da Silva", "528.111.272-40");
        client2 = new Client("Joana Meireles", "987.951.357-12");
        clientRequest = new ClientRequest("Joao da Silva", "528.111.272-40");
        account1 = new Account(LocalDateTime.now());
        account1.setId(1L);
        id =1;
        allClients = Arrays.asList(client1, client2);
    }

    @Test
    public void createTestCorrect() {
        when(accountService.create(any(Account.class))).thenReturn(account1);
        when(clientRepository.save(client1)).thenReturn(client1);
        ClientResponse clientResponse = clientService.create(clientRequest);
        assertNotNull(clientResponse);
    }

    @Test
    public void createTest_ClientExists() {
        when(accountService.create(any(Account.class))).thenReturn(account1);
        when(clientRepository.save(client1)).thenReturn(client1);
        when(clientRepository.findByCpf(anyString())).thenReturn(client1);
        thrown.expect(BusinessException.class);
        thrown.expectMessage("Cliente já possui cadastro no banco de dados");
        ClientResponse clientResponse = clientService.create(clientRequest);
        assertNotNull(clientResponse);
    }

    @Test
    public void findByAccountIdResponseTest_Ok() {
        when(clientRepository.findByAccountId(anyLong())).thenReturn(client1);
        ClientResponse clientResponse = clientService.findByAccountIdResponse(1L);
        assertEquals("528.111.272-40", clientResponse.getCpf());
        assertEquals("Joao da Silva", clientResponse.getName());
    }

    @Test
    public void findByAccountIdResponseTest_Invalid() {
        when(clientRepository.findByAccountId(anyLong())).thenReturn(null);
        thrown.expect(NonExistentException.class);
        thrown.expectMessage("Conta Inexistente");
        clientService.findByAccountIdResponse(1L);

    }

    @Test
    public void findByAccountIdTest_Found() {
        when(clientRepository.findByAccountId(anyLong())).thenReturn(client1);
        Client client = clientService.findByAccountId(2L);
        assertNotNull(client);
    }

    @Test
    public void findByAccountIdTest_NotFound() {
        when(clientRepository.findByAccountId(anyLong())).thenReturn(null);
        thrown.expect(NonExistentException.class);
        thrown.expectMessage("Conta Inexistente");
        clientService.findByAccountId(2L);
    }

    @Test
    public void findByCpfTest_Found() {
        when(clientRepository.findByCpf(anyString())).thenReturn(client1);
        ClientResponse clientResponse = clientService.findByCpf("528.111.272-40");
        assertEquals("528.111.272-40", clientResponse.getCpf());
        assertEquals("Joao da Silva", clientResponse.getName());
    }

    @Test
    public void findByCpfTest_NotFound() {
        when(clientRepository.findByCpf(anyString())).thenReturn(null);
        thrown.expect(NonExistentException.class);
        thrown.expectMessage("Conta Inexistente");
        clientService.findByCpf("528.111.272-40");
    }

    @Test
    public void updateTest_Ok() {

        when(clientRepository.save(client1)).thenReturn(client1);
        when(accountService.create(any(Account.class))).thenReturn(account1);
        when(clientRepository.findByAccountId(anyLong())).thenReturn(client1);
        ClientResponse clientResponse = clientService.update(clientRequest,1L);
        assertEquals("528.111.272-40", clientResponse.getCpf());
        assertEquals("Joao da Silva", clientResponse.getName());
    }

    @Test
    public void updateTest_NotExist() {

        when(clientRepository.save(client1)).thenReturn(client1);
        when(accountService.create(any(Account.class))).thenReturn(account1);
        when(clientRepository.findByAccountId(anyLong())).thenReturn(null);
        thrown.expect(NonExistentException.class);
        thrown.expectMessage("Conta Inexistente");
        clientService.update(clientRequest,1L);
    }
}
