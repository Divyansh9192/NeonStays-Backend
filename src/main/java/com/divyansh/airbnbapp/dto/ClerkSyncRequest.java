package com.divyansh.airbnbapp.dto;

import lombok.Data;

@Data
public class ClerkSyncRequest {
    private String clerkId;
    private String email;
    private String firstName;
    private String lastName;
    private String imageUrl;
    private String clerkToken;
}
