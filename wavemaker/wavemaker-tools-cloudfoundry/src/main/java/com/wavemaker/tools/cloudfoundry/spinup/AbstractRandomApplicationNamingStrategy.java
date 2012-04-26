/*
 *  Copyright (C) 2012 VMware, Inc. All rights reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.wavemaker.tools.cloudfoundry.spinup;

import java.security.SecureRandom;
import java.util.Random;

import com.wavemaker.tools.cloudfoundry.spinup.util.HexString;

/**
 * Abstract {@link ApplicationNamingStrategy} that generates application URLs based on a root name combined with random
 * characters. The application name is fixed.
 * 
 * @author Phillip Webb
 */
public abstract class AbstractRandomApplicationNamingStrategy implements ApplicationNamingStrategy {

    /**
     * Maximum length of the domain section (64 character, same as domain name standards)
     */
    private static final int MAX_NAME_LENGTH = 64;

    private static final Random RANDOM = new SecureRandom();

    protected final String applicationName;

    /**
     * Create a new {@link AbstractRandomApplicationNamingStrategy} instance.
     * 
     * @param applicationName the application name
     */
    public AbstractRandomApplicationNamingStrategy(String applicationName) {
        this.applicationName = applicationName;
    }

    @Override
    public boolean isMatch(ApplicationDetails applicationDetails) {
        return this.applicationName.equalsIgnoreCase(applicationDetails.getName());
    }

    @Override
    public ApplicationDetails newApplicationDetails(ApplicationNamingStrategyContext context) {
        String url = context.getControllerUrl();
        url = url.replace("https", "http");
        url = url.replace("api.", generateName(context) + ".");
        return new ApplicationDetails(this.applicationName, url);
    }

    public String generateName(){
        String name = replaceInvalidChars(this.applicationName);
        String random = generateRandom();
        int maxNameLength = MAX_NAME_LENGTH - random.length();
        if (name.length() > maxNameLength) {
            name = name.substring(0, maxNameLength);
        }
        return (name + random).toLowerCase();
    }

    private String generateName(ApplicationNamingStrategyContext context) {
        String name = getNameRoot(context);
        name = replaceInvalidChars(name);
        String random = generateRandom();
        int maxNameLength = MAX_NAME_LENGTH - random.length();
        if (name.length() > maxNameLength) {
            name = name.substring(0, maxNameLength);
        }
        return (name + random).toLowerCase();
    }

    protected abstract String getNameRoot(ApplicationNamingStrategyContext context);

    private String replaceInvalidChars(String name) {
        StringBuilder s = new StringBuilder(name.length());
        for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);
            if (isValidDomainNameChar(c)) {
                s.append(c);
            }
        }
        return s.toString();
    }

    private String generateRandom() {
        byte[] bytes = new byte[4];
        RANDOM.nextBytes(bytes);
        return HexString.toString(bytes);
    }

    private boolean isValidDomainNameChar(char c) {
        return Character.isLetterOrDigit(c) || c == '-';
    }

}
