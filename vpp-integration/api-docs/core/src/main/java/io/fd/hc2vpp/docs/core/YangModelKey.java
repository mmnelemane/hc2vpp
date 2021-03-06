/*
 * Copyright (c) 2017 Cisco and/or its affiliates.
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

package io.fd.hc2vpp.docs.core;import org.opendaylight.yangtools.yang.binding.YangModuleInfo;

final class YangModelKey {
    private final String namespace;
    private final String revision;

    YangModelKey(final YangModuleInfo moduleInfo) {
        this.namespace = moduleInfo.getNamespace();
        this.revision = moduleInfo.getRevision();
    }

    YangModelKey(final String namespace, final String revision) {
        this.namespace = namespace;
        this.revision = revision;
    }


    public String getNamespace() {
        return namespace;
    }

    public String getRevision() {
        return revision;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final YangModelKey that = (YangModelKey) o;

        if (namespace != null
                ? !namespace.equals(that.namespace)
                : that.namespace != null) {
            return false;
        }
        return revision != null
                ? revision.equals(that.revision)
                : that.revision == null;
    }

    @Override
    public int hashCode() {
        int result = namespace != null
                ? namespace.hashCode()
                : 0;
        result = 31 * result + (revision != null
                ? revision.hashCode()
                : 0);
        return result;
    }
}
