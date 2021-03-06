/**
 * Copyright 2018 Dynatrace LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.dynatrace.openkit.test.shared;

import com.dynatrace.openkit.api.Action;
import com.dynatrace.openkit.api.OpenKit;
import com.dynatrace.openkit.api.RootAction;
import com.dynatrace.openkit.api.Session;

public class ParallelActionTestShared {

    public static void test(OpenKit openKit, String ipAddress) {
        Session session = openKit.createSession(ipAddress);
        RootAction rootAction = session.enterAction("RootAction");

        Action parallelAction1 = rootAction.enterAction("ParallelAction-1");
        Action parallelAction2 = rootAction.enterAction("ParallelAction-2");
        Action parallelAction3 = rootAction.enterAction("ParallelAction-3");

        parallelAction1.leaveAction();
        parallelAction2.leaveAction();
        parallelAction3.leaveAction();

        rootAction.leaveAction();

        session.end();

        openKit.shutdown();
    }

}
