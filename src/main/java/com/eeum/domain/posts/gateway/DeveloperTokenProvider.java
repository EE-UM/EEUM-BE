package com.eeum.domain.posts.gateway;

import com.eeum.domain.posts.entity.DeveloperToken;

public interface DeveloperTokenProvider {

  DeveloperToken getToken();
}
