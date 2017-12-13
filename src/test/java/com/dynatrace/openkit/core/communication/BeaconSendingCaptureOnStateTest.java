package com.dynatrace.openkit.core.communication;

import com.dynatrace.openkit.core.SessionImpl;
import com.dynatrace.openkit.protocol.HTTPClient;
import com.dynatrace.openkit.protocol.StatusResponse;
import com.dynatrace.openkit.providers.HTTPClientProvider;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

public class BeaconSendingCaptureOnStateTest {

   // private AbstractBeaconSendingState mockState;
    private BeaconSendingContext mockContext;
    private StatusResponse mockResponse;
    private HTTPClient mockHTTPClient;
    private HTTPClientProvider mockHTTPClientProvider;
    private SessionImpl mockSession1Open;
    private SessionImpl mockSession2Open;
    private SessionImpl mockSession3Finished;
    private SessionImpl mockSession4Finished;

    @Before
    public void setUp() throws InterruptedException {

        mockSession1Open = mock(SessionImpl.class);
        mockSession2Open = mock(SessionImpl.class);
        mockSession3Finished = mock(SessionImpl.class);
        mockSession4Finished = mock(SessionImpl.class);
        //doReturn(new StatusResponse("OK", 200)).when(mockSession4Finished).sendBeacon(any(HTTPClientProvider.class), anyInt());
        when(mockSession1Open.sendBeacon(any(HTTPClientProvider.class), anyInt())).thenReturn(new StatusResponse("", 200));
        when(mockSession2Open.sendBeacon(any(HTTPClientProvider.class), anyInt())).thenReturn(new StatusResponse("", 404));

        mockResponse = mock(StatusResponse.class);

        mockHTTPClient = mock(HTTPClient.class);
        when(mockHTTPClient.sendStatusRequest()).thenReturn(mockResponse);

        mockContext = mock(BeaconSendingContext.class);
        when(mockContext.isTimeSyncSupported()).thenReturn(true);
        when(mockContext.getLastTimeSyncTime()).thenReturn(0L);
        when(mockContext.getCurrentTimestamp()).thenReturn(42L);
        when(mockContext.getAllOpenSessions()).thenReturn(new SessionImpl[]{mockSession1Open,mockSession2Open});
        when(mockContext.getNextFinishedSession()).thenReturn(mockSession3Finished).thenReturn(mockSession4Finished).thenReturn(null);
        when(mockContext.getHTTPClientProvider()).thenReturn(mockHTTPClientProvider);
    }

    @Test
    public void aBeaconSendingCaptureOnStateIsNotATerminalState(){
        //given
        BeaconSendingCaptureOnState target = new BeaconSendingCaptureOnState();

        //verify that BeaconSendingCaptureOffState is not a terminal state
        assertThat(target.isTerminalState(), is(false));
    }

    @Test
    public void aBeaconSendingCaptureOnStateHasTerminalStateBeaconSendingFlushSessions(){
        //given
        BeaconSendingCaptureOnState target = new BeaconSendingCaptureOnState();

        AbstractBeaconSendingState terminalState = target.getShutdownState();
        //verify that terminal state is BeaconSendingFlushSessions
        assertThat(terminalState, is(instanceOf(BeaconSendingFlushSessionsState.class)));
    }

    @Test
    public void aBeaconSendingCaptureOnStateTransitionsToTimeSyncStateWhenFirstTimeSyncRequired() throws InterruptedException {

        BeaconSendingCaptureOnState target = new BeaconSendingCaptureOnState();

        //given
        when(mockContext.isTimeSyncSupported()).thenReturn(true);
        when(mockContext.isCaptureOn()).thenReturn(false);
        when(mockContext.getLastTimeSyncTime()).thenReturn(-1L);

        // when calling execute
        target.execute(mockContext);

        // then verify that lastStatusCheckTime was updated and next state is time sync state
        verify(mockContext, times(1)).setNextState(org.mockito.Matchers.any(BeaconSendingTimeSyncState.class));
    }

    @Test
    public void aBeaconSendingCaptureOnStateTransitionsToTimeSyncStateWhenCheckIntervalPassed() throws InterruptedException {

        BeaconSendingCaptureOnState target = new BeaconSendingCaptureOnState();

        //given
        when(mockContext.isTimeSyncSupported()).thenReturn(true);
        when(mockContext.isCaptureOn()).thenReturn(true);
        when(mockContext.getLastTimeSyncTime()).thenReturn(0L);
        when(mockContext.getCurrentTimestamp()).thenReturn(7500000L);

        // when calling execute
        target.doExecute(mockContext);

        // then verify that lastStatusCheckTime was updated and next state is time sync state
        verify(mockContext, times(1)).setNextState(org.mockito.Matchers.any(BeaconSendingTimeSyncState.class));
    }

    @Test
    public void aBeaconSendingCaptureOnStateSendsFinishedSessions() throws InterruptedException{

        //given
        BeaconSendingCaptureOnState target = new BeaconSendingCaptureOnState();

        //when calling execute
        target.doExecute(mockContext);

        verify(mockSession3Finished, times(1)).sendBeacon(org.mockito.Matchers.any(HTTPClientProvider.class), org.mockito.Matchers.eq(2));
        verify(mockSession4Finished, times(1)).sendBeacon(org.mockito.Matchers.any(HTTPClientProvider.class), org.mockito.Matchers.eq(2));
    }

    @Test
    public void aBeaconSendingCaptureOnStateSendsOpenSessionsIfNotExpired() throws InterruptedException {
        //given
        BeaconSendingCaptureOnState target = new BeaconSendingCaptureOnState();

        //when calling execute
        target.doExecute(mockContext);

        verify(mockSession1Open, times(1)).sendBeacon(org.mockito.Matchers.any(HTTPClientProvider.class), org.mockito.Matchers.eq(2));
        verify(mockSession2Open, times(1)).sendBeacon(org.mockito.Matchers.any(HTTPClientProvider.class), org.mockito.Matchers.eq(2));
        verify(mockContext, times(1)).setLastOpenSessionBeaconSendTime(org.mockito.Matchers.anyLong());
    }

    @Test
    public void aBeaconSendingCaptureOnStateTransitionsToTimeSyncStateIfSessionExpired() throws InterruptedException{

        //given
        BeaconSendingCaptureOnState target = new BeaconSendingCaptureOnState();
        when(mockContext.getCurrentTimestamp()).thenReturn(72000042L);

        //when calling execute
        target.doExecute(mockContext);

        verify(mockContext, times(1)).isTimeSyncSupported();
        verify(mockContext, times(1)).getCurrentTimestamp();
        verify(mockContext, times(2)).getLastTimeSyncTime();
        verify(mockContext, times(1)).setNextState(org.mockito.Matchers.any(BeaconSendingTimeSyncState.class));
        verifyNoMoreInteractions(mockContext);
    }

    @Test
    public void aBeaconSendingCaptureOnStateTransitionsToCaptureOffStateWhenCapturingGotDisabled() throws InterruptedException {

        BeaconSendingCaptureOnState target = new BeaconSendingCaptureOnState();

        //given
        when(mockContext.isTimeSyncSupported()).thenReturn(true);
        when(mockContext.isCaptureOn()).thenReturn(false);

        // when calling execute
        target.execute(mockContext);

        // then verify that capturing is set to disabled
        verify(mockContext, times(1)).handleStatusResponse(org.mockito.Matchers.any(StatusResponse.class));
        verify(mockContext, times(1)).isCaptureOn();

        verify(mockContext, times(1)).setNextState(org.mockito.Matchers.any(BeaconSendingCaptureOffState.class));
    }
}
