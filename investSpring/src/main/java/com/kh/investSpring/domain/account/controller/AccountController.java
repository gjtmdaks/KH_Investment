package com.kh.investSpring.domain.account.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.Authentication;

import com.kh.investSpring.domain.account.dto.AccountAssetResponse;
import com.kh.investSpring.domain.account.dto.AccountSummaryDto;
import com.kh.investSpring.domain.account.service.AccountService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/account")
@RequiredArgsConstructor
public class AccountController {
	private final AccountService accountService;

    @GetMapping("/summary")
    public ResponseEntity<AccountSummaryDto> getAccountSummary(
    		Authentication authentication
    ) {
    	Long userNo = Long.valueOf(authentication.getName());
    	
        AccountSummaryDto accountSummary = accountService.getAccountSummary(userNo);

        return ResponseEntity.ok(accountSummary);
    }
    
    @GetMapping("/assets")
    public ResponseEntity<AccountAssetResponse> getAccountAssets(
            Authentication authentication
    ) {
        Long userNo = Long.valueOf(authentication.getName());

        AccountAssetResponse response = accountService.getAccountAssets(userNo);

        return ResponseEntity.ok(response);
    }
}
