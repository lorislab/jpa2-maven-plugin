/*
 * Copyright 2015 Andrej_Petras.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.lorislab.maven.jpa2.persistence;

import java.util.ArrayList;
import java.util.List;
import org.lorislab.maven.jpa2.persistence.jpa21.Persistence;

/**
 * The persistence modifier for version 2.1
 * 
 * @author Andrej_Petras
 */
public class PersistenceModel21 extends PersistenceModel<Persistence> {

    /**
     * The default constructor.
     */
    public PersistenceModel21() {
        super(Persistence.class);
    }

    @Override
    public String getVersion() {
        return "2.1";
    }
    
    

    @Override
    protected List<String> getPersistenceUnits(Persistence persistence) {
        List<String> result = new ArrayList<>();
        if (persistence != null) {
            List<Persistence.PersistenceUnit> units = persistence.getPersistenceUnit();
            if (units != null) {
                for (Persistence.PersistenceUnit unit : units) {
                    result.add(unit.getName());
                }
            }
        }
        return result;
    }
}
