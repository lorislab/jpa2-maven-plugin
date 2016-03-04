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

import java.nio.file.Path;
import java.util.List;
import org.lorislab.maven.jpa2.util.XMLUtil;

/**
 *
 * @author Andrej_Petras
 */
public abstract class PersistenceModel<T> {
    
    private final Class<T> clazz;

    private T persistence;
    
    public PersistenceModel(Class<T> clazz) {
        this.clazz = clazz;
    }
        
    public void loadPersistence(Path path) {
        persistence = XMLUtil.loadObject(path, clazz); 
    }
    
    public List<String> getPersistenceUnits() {
        return getPersistenceUnits(persistence);
    }
    
    protected abstract List<String> getPersistenceUnits(T persistence);
    
    public abstract String getVersion();
}
