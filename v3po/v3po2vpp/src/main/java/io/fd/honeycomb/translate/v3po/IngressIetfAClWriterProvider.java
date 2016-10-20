/*
 * Copyright (c) 2016 Cisco and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at:
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.fd.honeycomb.translate.v3po;

import com.google.inject.Inject;
import com.google.inject.Provider;
import io.fd.honeycomb.translate.v3po.interfaces.acl.ingress.IngressIetfAclWriter;
import io.fd.vpp.jvpp.core.future.FutureJVppCore;

class IngressIetfAClWriterProvider implements Provider<IngressIetfAclWriter> {

    private final FutureJVppCore jvpp;

    @Inject
    public IngressIetfAClWriterProvider(final FutureJVppCore jvpp) {
        this.jvpp = jvpp;
    }

    @Override
    public IngressIetfAclWriter get() {
        return new IngressIetfAclWriter(jvpp);
    }
}