package com.example.bankpoc.serviceTest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import com.example.bankpoc.base.BankBaseTest;
import com.example.bankpoc.exception.BusinessException;
import com.example.bankpoc.exception.NonExistentException;
import com.example.bankpoc.models.entity.Account;
import com.example.bankpoc.models.entity.CashOut;
import com.example.bankpoc.models.entity.Client;
import com.example.bankpoc.models.entity.Deposit;
import com.example.bankpoc.models.entity.Transaction;
import com.example.bankpoc.models.entity.Transfer;
import com.example.bankpoc.models.enums.TypeTransfer;
import com.example.bankpoc.models.request.CashoutRequest;
import com.example.bankpoc.models.request.DepositRequest;
import com.example.bankpoc.models.request.TransferRequest;
import com.example.bankpoc.models.response.CashoutResponse;
import com.example.bankpoc.models.response.DepositResponse;
import com.example.bankpoc.models.response.TransferResponse;
import com.example.bankpoc.models.response.TransfersResponse;
import com.example.bankpoc.service.implement.OperationServiceImpl;
import com.example.bankpoc.service.interfaceServ.AccountService;
import com.example.bankpoc.service.interfaceServ.CashOutService;
import com.example.bankpoc.service.interfaceServ.ClientService;
import com.example.bankpoc.service.interfaceServ.DepositService;
import com.example.bankpoc.service.interfaceServ.TransferService;

public class OperationServiceTest extends BankBaseTest {

    @Mock
    private ClientService clientService;

    @Mock
    private AccountService accountService;

    @Mock
    private TransferService transferService;

    @Mock
    private DepositService depositService;

    @Mock
    private CashOutService cashOutService;

    @InjectMocks
    private OperationServiceImpl operationService;

    private Client client1;
    private Account account1;
    private Account account2;
    private DepositRequest depositRequest;
    private DepositResponse depositResponse;
    private Deposit deposit1;
    private CashoutRequest cashoutRequest;
    private CashOut cashOut;
    private TransferRequest transferRequest;
    private Transfer transfer;
    private List<Deposit> deposits;
    private List<CashOut> cashOuts;
    private List<Transfer> transfers;


    @Before
    public void setUp() {
        client1 = new Client("Marcio Araujo","526.250.931-29");
        account1 = new Account(LocalDateTime.now());
        account1.setId(1L);
        account1.setBalance(200);

        account2 = new Account(LocalDateTime.now());
        account2.setId(2L);
        account2.setBalance(200);
        depositResponse = new DepositResponse(1L, 200, LocalDateTime.now(),
                TypeTransfer.DEPOSIT.name());
        depositRequest = new DepositRequest(1L, 200);
        deposit1 = new Deposit(depositRequest);

        cashoutRequest = new CashoutRequest(1L, 200);
        cashOut = new CashOut(1L, 200);

        transferRequest = new TransferRequest(1l,2l,200);

        transfer = new Transfer(transferRequest);

        deposits = new ArrayList<>();
        cashOuts = new ArrayList<>();
        transfers = new ArrayList<>();

        deposits.add(deposit1);
        cashOuts.add(cashOut);
        transfers.add(transfer);

    }

    @Test
    public void getBalanceTest_Ok() {
        when(clientService.findByAccountId(anyLong())).thenReturn(client1);
        when(accountService.findById(anyLong())).thenReturn(account1);

        String resp = operationService.getBalance(1L);
        assertEquals("{ \"Balance\" : "+ 200.0 + " } ",resp);
    }

    @Test
    public void depositTest_Ok() {
        when(accountService.findById(anyLong())).thenReturn(account1);
        when(depositService.create(any(Deposit.class))).thenReturn(deposit1);
        DepositResponse depositResponse1 = operationService.deposit(depositRequest);
        assertNotNull(depositResponse1);
    }

    @Test
    public void cashOutTest_Ok() {
        when(accountService.findById(anyLong())).thenReturn(account1);
        when(cashOutService.create(any(CashoutRequest.class))).thenReturn(cashOut);
        CashoutResponse cashoutResponse = operationService.cashOut(cashoutRequest);
        assertNotNull(cashoutResponse);
    }

    @Test
    public void cashOutTest_Error() {
        when(accountService.findById(anyLong())).thenReturn(account1);
        when(cashOutService.create(any(CashoutRequest.class))).thenReturn(cashOut);

        CashoutRequest cashoutRequest1 = new CashoutRequest(1L, 300);
        try {
            operationService.cashOut(cashoutRequest1);
        }
        catch (BusinessException exception) {
            assertEquals("Valor Invalido para saque",exception.getError());
        }
    }

    @Test
    public void transferTest_Ok() {
        when(accountService.findById(anyLong())).thenReturn(account1);
        when(accountService.findById(anyLong())).thenReturn(account2);
        when(transferService.transfer(any(TransferRequest.class))).thenReturn(transfer);
        TransferResponse transferResponse = operationService.transfer(transferRequest);
        assertNotNull(transferResponse);
    }


    @Test
    public void transferTest_InvalidValue() {
        transferRequest.setValue(300);
        when(accountService.findById(anyLong())).thenReturn(account1);
        when(accountService.findById(anyLong())).thenReturn(account2);
        when(transferService.transfer(any(TransferRequest.class))).thenReturn(transfer);
        try {
            operationService.transfer(transferRequest);
        }
        catch (BusinessException exception) {
            assertEquals("Saldo Insuficiente",exception.getError());
        }
    }

    @Test
    public void transferTest_InvalidTransaction() {
        transferRequest.setDepositAccountid(1L);
        transferRequest.setRecipientAccountid(1L);
        when(accountService.findById(anyLong())).thenReturn(account1);
        when(accountService.findById(anyLong())).thenReturn(account1);
        when(transferService.transfer(any(TransferRequest.class))).thenReturn(transfer);
        try {
            operationService.transfer(transferRequest);
        }
        catch (BusinessException exception) {
            assertEquals("Não é possivel fazer uma transação para a mesma conta",exception.getMessage());
        }
    }

    @Test
    public void getTransfers() {

        when(accountService.findById(anyLong())).thenReturn(account1);
        when(transferService.getTransfers(anyLong())).thenReturn(transfers);
        when(depositService.findCustomerDeposits(anyLong())).thenReturn(deposits);
        when(cashOutService.findCustomeCashOuts(anyLong())).thenReturn(cashOuts);
        List<Transfer> transactions = operationService.getTransfers(1L);
        assertNotNull(transactions);

    }

}
