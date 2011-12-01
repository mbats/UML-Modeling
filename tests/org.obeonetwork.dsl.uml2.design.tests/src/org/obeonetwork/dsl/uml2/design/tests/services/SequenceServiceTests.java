/*******************************************************************************
 * Copyright (c) 2009, 2011 Obeo.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Obeo - initial API and implementation
 *******************************************************************************/
package org.obeonetwork.dsl.uml2.design.tests.services;

import java.util.List;

import junit.framework.TestCase;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.uml2.uml.Behavior;
import org.eclipse.uml2.uml.BehaviorExecutionSpecification;
import org.eclipse.uml2.uml.ExecutionOccurrenceSpecification;
import org.eclipse.uml2.uml.ExecutionSpecification;
import org.eclipse.uml2.uml.Interaction;
import org.eclipse.uml2.uml.InteractionFragment;
import org.eclipse.uml2.uml.Lifeline;
import org.eclipse.uml2.uml.Message;
import org.eclipse.uml2.uml.MessageOccurrenceSpecification;
import org.eclipse.uml2.uml.Model;
import org.eclipse.uml2.uml.OccurrenceSpecification;
import org.eclipse.uml2.uml.Operation;
import org.obeonetwork.dsl.uml2.design.services.SequenceServices;
import org.obeonetwork.dsl.uml2.design.tests.Activator;

/**
 * Unit tests on sequence services.
 * 
 * @author Gonzague Reydet <a href="mailto:gonzague.reydet@obeo.fr">gonzague.reydet@obeo.fr</a>
 */
public class SequenceServiceTests extends TestCase {
	/**
	 * The test model URI.
	 */
	private static final String RESOURCE_URI = Activator.PLUGIN_ID + "/resources/Test.uml";

	/**
	 * The sequence services instance.
	 */
	private SequenceServices sequenceServices;

	/**
	 * Constructor.
	 */
	private Interaction getInteraction(String resourceUri, String name) {
		final ResourceSet rset = new ResourceSetImpl();
		final Resource resource = rset.getResource(URI.createPlatformPluginURI(resourceUri, true), true);

		return (Interaction)((Model)resource.getContents().get(0)).getOwnedMember(name);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void setUp() throws Exception {
		super.setUp();

		sequenceServices = new SequenceServices();
	}

	/**
	 * Test the executionSemanticCandidates() service against {@link Lifeline}.
	 */
	public void testExecutionSemanticCandidatesLifeline() {
		Interaction interaction = getInteraction(RESOURCE_URI, "Scenario_5");
		Lifeline lifeline = interaction.getLifeline("consumers");
		final List<ExecutionSpecification> executions = sequenceServices
				.executionSemanticCandidates(lifeline);

		assertEquals(3, executions.size());
		assertEquals("compute", executions.get(0).getName());
		assertEquals("BehaviorExecution_2", executions.get(1).getName());
		assertEquals("BehaviorExecution_5", executions.get(2).getName());
	}

	/**
	 * Test the executionSemanticCandidates() service against {@link ExecutionSpecification}.
	 */
	public void testExecutionSemanticCandidatesExecutionSpecification() {
		Interaction interaction = getInteraction(RESOURCE_URI, "Scenario_5");
		BehaviorExecutionSpecification execution = (BehaviorExecutionSpecification)interaction
				.getFragment("compute");
		final List<ExecutionSpecification> subExecution = sequenceServices
				.executionSemanticCandidates(execution);

		assertEquals(1, subExecution.size());
		assertEquals("BehaviorExecution_1", subExecution.get(0).getName());

		BehaviorExecutionSpecification execution2 = (BehaviorExecutionSpecification)interaction
				.getFragment("BehaviorExecution_2");
		final List<ExecutionSpecification> subExecution2 = sequenceServices
				.executionSemanticCandidates(execution2);
		assertEquals("BehaviorExecution_3", subExecution2.get(0).getName());
		assertEquals("BehaviorExecution_4", subExecution2.get(1).getName());
	}

	/**
	 * Test findOccurrenceSpecificationContext() service.
	 * 
	 * @throws Exception
	 *             in case of error.
	 */
	public void testFindOccurenceSpecificationContext() throws Exception {
		OccurrenceSpecification executionOccurrence;
		Interaction interaction = getInteraction(RESOURCE_URI, "Scenario_6");
		executionOccurrence = (OccurrenceSpecification)interaction.getFragment("compute_start");
		assertEquals("consumers", sequenceServices.findOccurrenceSpecificationContext(executionOccurrence)
				.getName());
		executionOccurrence = (OccurrenceSpecification)interaction.getFragment("compute_finish");
		assertEquals("consumers", sequenceServices.findOccurrenceSpecificationContext(executionOccurrence)
				.getName());

		executionOccurrence = (OccurrenceSpecification)interaction.getFragment("get_receiver");
		assertEquals("get", sequenceServices.findOccurrenceSpecificationContext(executionOccurrence)
				.getName());

		executionOccurrence = (OccurrenceSpecification)interaction.getFragment("get_reply_sender");
		assertEquals("get", sequenceServices.findOccurrenceSpecificationContext(executionOccurrence)
				.getName());

		executionOccurrence = (OccurrenceSpecification)interaction.getFragment("get_reply_receiver");
		assertEquals("compute", sequenceServices.findOccurrenceSpecificationContext(executionOccurrence)
				.getName());

		executionOccurrence = (OccurrenceSpecification)interaction.getFragment("BehaviorExecution_3_start");
		assertEquals("compute", sequenceServices.findOccurrenceSpecificationContext(executionOccurrence)
				.getName());

		executionOccurrence = (OccurrenceSpecification)interaction.getFragment("produce_sender");
		assertEquals("BehaviorExecution_3",
				sequenceServices.findOccurrenceSpecificationContext(executionOccurrence).getName());
	}

	/**
	 * Test createExecution service. Create a new execution on an empty lifeline.
	 * 
	 * @throws Exception
	 *             in case of error
	 */
	public void testCreateExecution() throws Exception {
		Interaction interaction = getInteraction(RESOURCE_URI, "Scenario_0");
		Lifeline lifeline = interaction.getLifeline("consumers");
		sequenceServices.createExecution(interaction, lifeline, null);

		List<InteractionFragment> fragments = interaction.getFragments();
		assertEquals(3, fragments.size());
		assertEquals("Opaque behavior does not exist", "BehaviorExecution_0", interaction.getOwnedBehaviors()
				.get(0).getName());
		assertTrue(fragments.get(0) instanceof ExecutionOccurrenceSpecification);
		assertEquals("BehaviorExecution_0_start", fragments.get(0).getName());
		assertTrue(fragments.get(2) instanceof ExecutionOccurrenceSpecification);
		assertEquals("BehaviorExecution_0_finish", fragments.get(2).getName());
		assertTrue(fragments.get(1) instanceof BehaviorExecutionSpecification);
		assertEquals("BehaviorExecution_0", fragments.get(1).getName());
		assertEquals(((BehaviorExecutionSpecification)fragments.get(1)).getStart(), fragments.get(0));
		assertEquals(((BehaviorExecutionSpecification)fragments.get(1)).getFinish(), fragments.get(2));
		assertNotNull(((BehaviorExecutionSpecification)fragments.get(1)).getCovered(lifeline.getName()));
		assertEquals(interaction.getOwnedBehaviors().get(0),
				((BehaviorExecutionSpecification)fragments.get(1)).getBehavior());
	}

	/**
	 * Test createExecution service. Import an execution on an empty lifeline.
	 * 
	 * @throws Exception
	 *             in case of error
	 */
	public void testImportExecution() throws Exception {
		Interaction interaction = getInteraction(RESOURCE_URI, "Scenario_0");
		Lifeline lifeline = interaction.getLifeline("consumers");
		Operation operation = (Operation)lifeline.getRepresents().getType().getOwnedElements().get(0);
		sequenceServices.createExecution(interaction, lifeline, operation, null);

		List<InteractionFragment> fragments = interaction.getFragments();
		assertEquals(3, fragments.size());
		assertEquals("Opaque behavior does not exist", "compute", interaction.getOwnedBehaviors().get(0)
				.getName());
		assertTrue(fragments.get(0) instanceof ExecutionOccurrenceSpecification);
		assertEquals("compute_start", fragments.get(0).getName());
		assertTrue(fragments.get(2) instanceof ExecutionOccurrenceSpecification);
		assertEquals("compute_finish", fragments.get(2).getName());
		assertTrue(fragments.get(1) instanceof BehaviorExecutionSpecification);
		assertEquals("compute", fragments.get(1).getName());
		assertEquals(((BehaviorExecutionSpecification)fragments.get(1)).getStart(), fragments.get(0));
		assertEquals(((BehaviorExecutionSpecification)fragments.get(1)).getFinish(), fragments.get(2));
		assertNotNull(((BehaviorExecutionSpecification)fragments.get(1)).getCovered(lifeline.getName()));
		assertEquals(interaction.getOwnedBehaviors().get(0),
				((BehaviorExecutionSpecification)fragments.get(1)).getBehavior());
	}

	/**
	 * Test createExecution service. Create an execution on an existing execution.
	 * 
	 * @throws Exception
	 *             in case of error
	 */
	public void testCreateExecutionOnExecution() throws Exception {
		Interaction interaction = getInteraction(RESOURCE_URI, "Scenario_1");
		Lifeline lifeline = interaction.getLifeline("consumers");
		ExecutionSpecification execution = (ExecutionSpecification)interaction.getFragment("compute");
		sequenceServices.createExecution(interaction, lifeline, execution);

		List<InteractionFragment> fragments = interaction.getFragments();
		assertEquals(6, fragments.size());
		assertEquals("Opaque behavior does not exist", "BehaviorExecution_1", interaction.getOwnedBehaviors()
				.get(1).getName());
		assertTrue(fragments.get(2) instanceof ExecutionOccurrenceSpecification);
		assertEquals("BehaviorExecution_1_start", fragments.get(2).getName());
		assertTrue(fragments.get(4) instanceof ExecutionOccurrenceSpecification);
		assertEquals("BehaviorExecution_1_finish", fragments.get(4).getName());
		assertTrue(fragments.get(3) instanceof BehaviorExecutionSpecification);
		assertEquals("BehaviorExecution_1", fragments.get(3).getName());
		assertEquals(((BehaviorExecutionSpecification)fragments.get(3)).getStart(), fragments.get(2));
		assertEquals(((BehaviorExecutionSpecification)fragments.get(3)).getFinish(), fragments.get(4));
		assertNotNull(((BehaviorExecutionSpecification)fragments.get(3)).getCovered(lifeline.getName()));
		assertEquals(interaction.getOwnedBehaviors().get(1),
				((BehaviorExecutionSpecification)fragments.get(3)).getBehavior());
	}

	/**
	 * Test createAsynchronousMessage service. Create an asynchronous message between to empty lifelines.
	 * 
	 * @throws Exception
	 *             in case of error
	 */
	public void testCreateAsynchronousMessage() throws Exception {
		Interaction interaction = getInteraction(RESOURCE_URI, "Scenario_2");
		Lifeline source = interaction.getLifeline("consumers");
		Lifeline target = interaction.getLifeline("producers");
		sequenceServices.createAsynchronousMessage(interaction, source, target, null, null);

		List<InteractionFragment> fragments = interaction.getFragments();
		assertEquals(2, fragments.size());
		assertEquals("Message does not exist", "Message_0", interaction.getMessages().get(0).getName());
		assertTrue(fragments.get(0) instanceof MessageOccurrenceSpecification);
		assertEquals("Message_0_sender", fragments.get(0).getName());
		assertTrue(fragments.get(1) instanceof MessageOccurrenceSpecification);
		assertEquals("Message_0_receiver", fragments.get(1).getName());
		assertEquals(((Message)interaction.getMessages().get(0)).getSendEvent(), fragments.get(0));
		assertEquals(((Message)interaction.getMessages().get(0)).getReceiveEvent(), fragments.get(1));
		assertNotNull(((MessageOccurrenceSpecification)fragments.get(0)).getCovered(source.getName()));
		assertNotNull(((MessageOccurrenceSpecification)fragments.get(1)).getCovered(target.getName()));
	}

	/**
	 * Test createAsynchronousMessage service. Create an asynchronous message between to empty lifelines.
	 * 
	 * @throws Exception
	 *             in case of error
	 */
	public void testCreateAsynchronousMessageOnExecution() throws Exception {
		Interaction interaction = getInteraction(RESOURCE_URI, "Scenario_3");
		Lifeline source = interaction.getLifeline("consumers");
		Lifeline target = interaction.getLifeline("producers");
		ExecutionSpecification execution = (ExecutionSpecification)interaction.getFragment("compute");

		sequenceServices.createAsynchronousMessage(interaction, execution, target, execution.getStart(),
				execution.getStart());

		List<InteractionFragment> fragments = interaction.getFragments();
		assertEquals(5, fragments.size());

		InteractionFragment sourceExecutionStart = fragments.get(0);
		InteractionFragment sourceExecution = fragments.get(1);
		InteractionFragment messageSend = fragments.get(2);
		InteractionFragment messageReceive = fragments.get(3);
		InteractionFragment sourceExecutionFinish = fragments.get(4);

		// Execution
		assertEquals("Opaque behavior does not exist", "compute", interaction.getOwnedBehaviors().get(0)
				.getName());
		assertTrue(sourceExecutionStart instanceof ExecutionOccurrenceSpecification);
		assertEquals("compute_start", sourceExecutionStart.getName());
		assertTrue(sourceExecutionFinish instanceof ExecutionOccurrenceSpecification);
		assertEquals("compute_finish", sourceExecutionFinish.getName());
		assertTrue(sourceExecution instanceof BehaviorExecutionSpecification);
		assertEquals("compute", sourceExecution.getName());
		assertEquals(((BehaviorExecutionSpecification)sourceExecution).getStart(), sourceExecutionStart);
		assertEquals(((BehaviorExecutionSpecification)sourceExecution).getFinish(), sourceExecutionFinish);
		assertNotNull(((BehaviorExecutionSpecification)sourceExecution).getCovered(source.getName()));
		assertEquals(interaction.getOwnedBehaviors().get(0),
				((BehaviorExecutionSpecification)sourceExecution).getBehavior());

		// Message
		assertEquals("Message does not exist", "Message_0", interaction.getMessages().get(0).getName());
		assertTrue(messageSend instanceof MessageOccurrenceSpecification);
		assertEquals("Message_0_sender", messageSend.getName());
		assertTrue(messageReceive instanceof MessageOccurrenceSpecification);
		assertEquals("Message_0_receiver", messageReceive.getName());
		assertEquals(((Message)interaction.getMessages().get(0)).getSendEvent(), messageSend);
		assertEquals(((Message)interaction.getMessages().get(0)).getReceiveEvent(), messageReceive);
		assertNotNull(((MessageOccurrenceSpecification)messageSend).getCovered(source.getName()));
		assertNotNull(((MessageOccurrenceSpecification)messageReceive).getCovered(target.getName()));
	}

	/**
	 * Test createAsynchronousMessage service. Import an asynchronous message between to empty lifelines.
	 * 
	 * @throws Exception
	 *             in case of error
	 */
	public void testImportAsynchronousMessage() throws Exception {
		Interaction interaction = getInteraction(RESOURCE_URI, "Scenario_2");
		Lifeline source = interaction.getLifeline("consumers");
		Lifeline target = interaction.getLifeline("producers");
		Operation operation = (Operation)source.getRepresents().getType().getOwnedElements().get(0);

		sequenceServices.createAsynchronousMessage(interaction, source, target, operation, null, null);

		List<InteractionFragment> fragments = interaction.getFragments();
		assertEquals(4, fragments.size());
		// Message
		assertEquals("Message does not exist", "compute", interaction.getMessages().get(0).getName());
		assertTrue(fragments.get(0) instanceof MessageOccurrenceSpecification);
		assertEquals("compute_sender", fragments.get(0).getName());
		assertTrue(fragments.get(1) instanceof MessageOccurrenceSpecification);
		assertEquals("compute_receiver", fragments.get(1).getName());
		assertEquals(((Message)interaction.getMessages().get(0)).getSendEvent(), fragments.get(0));
		assertEquals(((Message)interaction.getMessages().get(0)).getReceiveEvent(), fragments.get(1));
		assertNotNull(((MessageOccurrenceSpecification)fragments.get(0)).getCovered(source.getName()));
		assertNotNull(((MessageOccurrenceSpecification)fragments.get(1)).getCovered(target.getName()));

		// Execution
		assertEquals("Opaque behavior does not exist", "compute", interaction.getOwnedBehaviors().get(0)
				.getName());
		assertTrue(fragments.get(3) instanceof ExecutionOccurrenceSpecification);
		assertEquals("compute_finish", fragments.get(3).getName());
		assertTrue(fragments.get(2) instanceof BehaviorExecutionSpecification);
		assertEquals("compute", fragments.get(2).getName());
		assertEquals(((BehaviorExecutionSpecification)fragments.get(2)).getStart(), fragments.get(1));
		assertEquals(((BehaviorExecutionSpecification)fragments.get(2)).getFinish(), fragments.get(3));
		assertNotNull(((BehaviorExecutionSpecification)fragments.get(2)).getCovered(target.getName()));
		assertEquals(interaction.getOwnedBehaviors().get(0),
				((BehaviorExecutionSpecification)fragments.get(2)).getBehavior());
	}

	/**
	 * Test createAsynchronousMessage service. Import an asynchronous message between an existing execution
	 * and an empty lifeline.
	 * 
	 * @throws Exception
	 *             in case of error
	 */
	public void testImportAsynchronousMessageOnExecution() throws Exception {
		Interaction interaction = getInteraction(RESOURCE_URI, "Scenario_3");
		Lifeline source = interaction.getLifeline("consumers");
		Lifeline target = interaction.getLifeline("producers");
		Operation operation = (Operation)target.getRepresents().getType().getOwnedElements().get(1);
		ExecutionSpecification execution = (ExecutionSpecification)interaction.getFragment("compute");

		sequenceServices.createAsynchronousMessage(interaction, execution, target, operation,
				execution.getStart(), execution.getStart());

		List<InteractionFragment> fragments = interaction.getFragments();
		assertEquals(7, fragments.size());

		InteractionFragment sourceExecutionStart = fragments.get(0);
		InteractionFragment sourceExecution = fragments.get(1);
		InteractionFragment messageSend = fragments.get(2);
		InteractionFragment messageReceive = fragments.get(3);
		InteractionFragment targetExecution = fragments.get(4);
		InteractionFragment targetExecutionFinish = fragments.get(5);
		InteractionFragment sourceExecutionFinish = fragments.get(6);

		// Execution compute
		assertEquals("Opaque behavior does not exist", "compute", interaction.getOwnedBehaviors().get(0)
				.getName());
		assertTrue(sourceExecutionStart instanceof ExecutionOccurrenceSpecification);
		assertEquals("compute_start", sourceExecutionStart.getName());
		assertTrue(sourceExecutionFinish instanceof ExecutionOccurrenceSpecification);
		assertEquals("compute_finish", sourceExecutionFinish.getName());
		assertTrue(sourceExecution instanceof BehaviorExecutionSpecification);
		assertEquals("compute", sourceExecution.getName());
		assertEquals(((BehaviorExecutionSpecification)sourceExecution).getStart(), sourceExecutionStart);
		assertEquals(((BehaviorExecutionSpecification)sourceExecution).getFinish(), sourceExecutionFinish);
		assertNotNull(((BehaviorExecutionSpecification)sourceExecution).getCovered(source.getName()));
		assertEquals(interaction.getOwnedBehaviors().get(0),
				((BehaviorExecutionSpecification)sourceExecution).getBehavior());

		// Message get
		assertEquals("Message does not exist", "get", interaction.getMessages().get(0).getName());
		assertTrue(messageSend instanceof MessageOccurrenceSpecification);
		assertEquals("get_sender", messageSend.getName());
		assertTrue(messageReceive instanceof MessageOccurrenceSpecification);
		assertEquals("get_receiver", messageReceive.getName());
		assertEquals(((Message)interaction.getMessages().get(0)).getSendEvent(), messageSend);
		assertEquals(((Message)interaction.getMessages().get(0)).getReceiveEvent(), messageReceive);
		assertNotNull(((MessageOccurrenceSpecification)messageSend).getCovered(source.getName()));
		assertNotNull(((MessageOccurrenceSpecification)messageReceive).getCovered(target.getName()));

		// Execution get
		assertEquals("Opaque behavior does not exist", "get", interaction.getOwnedBehaviors().get(1)
				.getName());
		assertTrue(targetExecution instanceof BehaviorExecutionSpecification);
		assertEquals("get", targetExecution.getName());
		assertEquals(((BehaviorExecutionSpecification)targetExecution).getStart(), messageReceive);
		assertTrue(targetExecutionFinish instanceof ExecutionOccurrenceSpecification);
		assertEquals("get_finish", targetExecutionFinish.getName());
		assertEquals(((BehaviorExecutionSpecification)targetExecution).getFinish(), targetExecutionFinish);
		assertNotNull(((BehaviorExecutionSpecification)targetExecution).getCovered(target.getName()));
		assertEquals(interaction.getOwnedBehaviors().get(1),
				((BehaviorExecutionSpecification)targetExecution).getBehavior());
	}

	/**
	 * Test createAsynchronousMessage service. Import two asynchronous messages between an existing execution
	 * and an empty lifeline.
	 * 
	 * @throws Exception
	 *             in case of error
	 */
	public void testImportTwoAsynchronousMessagesOnExecution() throws Exception {
		Interaction interaction = getInteraction(RESOURCE_URI, "Scenario_7");
		Lifeline source = interaction.getLifeline("consumers");
		Lifeline target = interaction.getLifeline("producers");
		Operation operation = (Operation)target.getRepresents().getType().getOwnedElements().get(0);
		ExecutionSpecification execution = (ExecutionSpecification)interaction.getFragment("compute");
		ExecutionSpecification execution2 = (ExecutionSpecification)interaction.getFragment("get");
		sequenceServices.createAsynchronousMessage(interaction, execution, target, operation,
				execution2.getFinish(), execution2.getFinish());

		List<InteractionFragment> fragments = interaction.getFragments();
		assertEquals(11, fragments.size());

		InteractionFragment sourceExecutionStart = fragments.get(0);
		InteractionFragment sourceExecution = fragments.get(1);
		InteractionFragment messageSend = fragments.get(2);
		InteractionFragment messageReceive = fragments.get(3);
		InteractionFragment targetExecution = fragments.get(4);
		InteractionFragment targetExecutionFinish = fragments.get(5);
		InteractionFragment messageSend2 = fragments.get(6);
		InteractionFragment messageReceive2 = fragments.get(7);
		InteractionFragment targetExecution2 = fragments.get(8);
		InteractionFragment targetExecution2Finish = fragments.get(9);
		InteractionFragment sourceExecutionFinish = fragments.get(10);

		// Execution compute
		assertEquals("Opaque behavior does not exist", "compute", interaction.getOwnedBehaviors().get(0)
				.getName());
		assertTrue(sourceExecutionStart instanceof ExecutionOccurrenceSpecification);
		assertEquals("compute_start", sourceExecutionStart.getName());
		assertTrue(sourceExecutionFinish instanceof ExecutionOccurrenceSpecification);
		assertEquals("compute_finish", sourceExecutionFinish.getName());
		assertTrue(sourceExecution instanceof BehaviorExecutionSpecification);
		assertEquals("compute", sourceExecution.getName());
		assertEquals(((BehaviorExecutionSpecification)sourceExecution).getStart(), sourceExecutionStart);
		assertEquals(((BehaviorExecutionSpecification)sourceExecution).getFinish(), sourceExecutionFinish);
		assertNotNull(((BehaviorExecutionSpecification)sourceExecution).getCovered(source.getName()));
		assertEquals(interaction.getOwnedBehaviors().get(0),
				((BehaviorExecutionSpecification)sourceExecution).getBehavior());

		// Message get
		Message message = (Message)interaction.getMessages().get(0);
		assertEquals("Message does not exist", "get", message.getName());
		assertTrue(messageSend instanceof MessageOccurrenceSpecification);
		assertEquals("get_sender", messageSend.getName());
		assertTrue(messageReceive instanceof MessageOccurrenceSpecification);
		assertEquals("get_receiver", messageReceive.getName());
		assertEquals(message.getSendEvent(), messageSend);
		assertEquals(message.getReceiveEvent(), messageReceive);
		assertNotNull(((MessageOccurrenceSpecification)messageSend).getCovered(source.getName()));
		assertNotNull(((MessageOccurrenceSpecification)messageReceive).getCovered(target.getName()));

		// Execution get
		assertEquals("Opaque behavior does not exist", "get", interaction.getOwnedBehaviors().get(1)
				.getName());
		assertTrue(targetExecution instanceof BehaviorExecutionSpecification);
		assertEquals("get", targetExecution.getName());
		assertEquals(((BehaviorExecutionSpecification)targetExecution).getStart(), messageReceive);
		assertTrue(targetExecutionFinish instanceof ExecutionOccurrenceSpecification);
		assertEquals("get_finish", targetExecutionFinish.getName());
		assertEquals(((BehaviorExecutionSpecification)targetExecution).getFinish(), targetExecutionFinish);
		assertNotNull(((BehaviorExecutionSpecification)targetExecution).getCovered(target.getName()));
		assertEquals(interaction.getOwnedBehaviors().get(1),
				((BehaviorExecutionSpecification)targetExecution).getBehavior());

		// Message produce
		Message message2 = (Message)interaction.getMessages().get(1);
		assertEquals("Message does not exist", "produce", interaction.getMessages().get(1).getName());
		assertTrue(messageSend2 instanceof MessageOccurrenceSpecification);
		assertEquals("produce_sender", messageSend2.getName());
		assertTrue(messageReceive2 instanceof MessageOccurrenceSpecification);
		assertEquals("produce_receiver", messageReceive2.getName());
		assertEquals(message2.getSendEvent(), messageSend2);
		assertEquals(message2.getReceiveEvent(), messageReceive2);
		assertNotNull(((MessageOccurrenceSpecification)messageSend2).getCovered(source.getName()));
		assertNotNull(((MessageOccurrenceSpecification)messageReceive2).getCovered(target.getName()));

		// Execution produce
		assertEquals("Opaque behavior does not exist", "produce", interaction.getOwnedBehaviors().get(2)
				.getName());
		assertTrue(targetExecution2 instanceof BehaviorExecutionSpecification);
		assertEquals("produce", targetExecution2.getName());
		assertEquals(((BehaviorExecutionSpecification)targetExecution2).getStart(), messageReceive2);
		assertTrue(targetExecution2Finish instanceof ExecutionOccurrenceSpecification);
		assertEquals("produce_finish", targetExecution2Finish.getName());
		assertEquals(((BehaviorExecutionSpecification)targetExecution2).getFinish(), targetExecution2Finish);
		assertNotNull(((BehaviorExecutionSpecification)targetExecution2).getCovered(target.getName()));
		assertEquals(interaction.getOwnedBehaviors().get(2),
				((BehaviorExecutionSpecification)targetExecution2).getBehavior());
	}

	/**
	 * Test createAsynchronousMessage service. Import an asynchronous message between an existing execution
	 * and an empty lifeline.
	 * 
	 * @throws Exception
	 *             in case of error
	 */
	public void testCreateAsynchronousMessageOnExecutionToExecution() throws Exception {
		// Interaction scenario_4
		Interaction interaction = getInteraction(RESOURCE_URI, "Scenario_4");
		Lifeline source = interaction.getLifeline("consumers");
		Lifeline target = interaction.getLifeline("producers");
		// SourceFragment compute behavior
		ExecutionSpecification executionSource = (ExecutionSpecification)interaction.getFragment("compute");
		// TargetFragment get behavior
		ExecutionSpecification executionTarget = (ExecutionSpecification)interaction.getFragment("get");
		// Operation null
		// StartingElementPredecessor compute_start
		// FinishingElementPredecessor compute_start
		sequenceServices.createAsynchronousMessage(interaction, executionSource, executionTarget,
				executionSource.getStart(), executionSource.getStart());

		List<InteractionFragment> fragments = interaction.getFragments();
		assertEquals(7, fragments.size());

		InteractionFragment sourceExecutionStart = fragments.get(0);
		InteractionFragment sourceExecution = fragments.get(1);
		InteractionFragment messageSend = fragments.get(2);
		InteractionFragment messageReceive = fragments.get(3);
		InteractionFragment targetExecution = fragments.get(4);
		InteractionFragment targetExecutionFinish = fragments.get(5);
		InteractionFragment sourceExecutionFinish = fragments.get(6);

		// Execution compute
		assertEquals("Opaque behavior does not exist", "compute", interaction.getOwnedBehaviors().get(0)
				.getName());

		assertTrue(sourceExecutionStart instanceof ExecutionOccurrenceSpecification);
		assertEquals("compute_start", sourceExecutionStart.getName());
		assertTrue(sourceExecutionFinish instanceof ExecutionOccurrenceSpecification);
		assertEquals("compute_finish", sourceExecutionFinish.getName());
		assertTrue(sourceExecution instanceof BehaviorExecutionSpecification);
		assertEquals("compute", sourceExecution.getName());
		assertEquals(((BehaviorExecutionSpecification)sourceExecution).getStart(), sourceExecutionStart);
		assertEquals(((BehaviorExecutionSpecification)sourceExecution).getFinish(), sourceExecutionFinish);
		assertNotNull(((BehaviorExecutionSpecification)sourceExecution).getCovered(source.getName()));
		assertEquals(interaction.getOwnedBehaviors().get(0),
				((BehaviorExecutionSpecification)sourceExecution).getBehavior());

		// Message get
		assertEquals("Message does not exist", "get", interaction.getMessages().get(0).getName());
		assertTrue(messageSend instanceof MessageOccurrenceSpecification);
		assertEquals("get_sender", messageSend.getName());
		assertTrue(messageReceive instanceof MessageOccurrenceSpecification);
		assertEquals("get_receiver", messageReceive.getName());
		Message message = (Message)interaction.getMessages().get(0);
		assertEquals(message.getSendEvent(), messageSend);
		assertEquals(message.getReceiveEvent(), messageReceive);
		assertNotNull(((MessageOccurrenceSpecification)messageSend).getCovered(source.getName()));
		assertNotNull(((MessageOccurrenceSpecification)messageReceive).getCovered(target.getName()));

		// Execution get
		assertEquals("Opaque behavior does not exist", "get", interaction.getOwnedBehaviors().get(1)
				.getName());
		assertTrue(targetExecution instanceof BehaviorExecutionSpecification);
		assertEquals("get", targetExecution.getName());
		assertEquals(((BehaviorExecutionSpecification)targetExecution).getStart(), messageReceive);
		assertTrue(targetExecutionFinish instanceof ExecutionOccurrenceSpecification);
		assertEquals("get_finish", targetExecutionFinish.getName());
		assertEquals(((BehaviorExecutionSpecification)targetExecution).getFinish(), targetExecutionFinish);
		assertNotNull(((BehaviorExecutionSpecification)targetExecution).getCovered(target.getName()));
		assertEquals(interaction.getOwnedBehaviors().get(1),
				((BehaviorExecutionSpecification)targetExecution).getBehavior());
	}

	/**
	 * Test createSynchronousMessage service. Create a synchronous message between to empty lifelines.
	 * 
	 * @throws Exception
	 *             in case of error
	 */
	public void testCreateSynchronousMessage() throws Exception {
		Interaction interaction = getInteraction(RESOURCE_URI, "Scenario_2");
		Lifeline source = interaction.getLifeline("consumers");
		Lifeline target = interaction.getLifeline("producers");
		sequenceServices.createSynchronousMessage(interaction, source, target, null, null);

		List<InteractionFragment> fragments = interaction.getFragments();
		assertEquals(4, fragments.size());
		// Message
		assertEquals("Message does not exist", "Message_0", interaction.getMessages().get(0).getName());
		assertTrue(fragments.get(0) instanceof MessageOccurrenceSpecification);
		assertEquals("Message_0_sender", fragments.get(0).getName());
		assertTrue(fragments.get(1) instanceof MessageOccurrenceSpecification);
		assertEquals("Message_0_receiver", fragments.get(1).getName());
		assertEquals(((Message)interaction.getMessages().get(0)).getSendEvent(), fragments.get(0));
		assertEquals(((Message)interaction.getMessages().get(0)).getReceiveEvent(), fragments.get(1));
		assertNotNull(((MessageOccurrenceSpecification)fragments.get(0)).getCovered(source.getName()));
		assertNotNull(((MessageOccurrenceSpecification)fragments.get(1)).getCovered(target.getName()));

		// Reply message
		assertEquals("Message does not exist", "Message_0_reply", interaction.getMessages().get(1).getName());
		assertTrue(fragments.get(2) instanceof MessageOccurrenceSpecification);
		assertEquals("Message_0_reply_sender", fragments.get(2).getName());
		assertTrue(fragments.get(3) instanceof MessageOccurrenceSpecification);
		assertEquals("Message_0_reply_receiver", fragments.get(3).getName());
		assertEquals(((Message)interaction.getMessages().get(1)).getSendEvent(), fragments.get(2));
		assertEquals(((Message)interaction.getMessages().get(1)).getReceiveEvent(), fragments.get(3));
		assertNotNull(((MessageOccurrenceSpecification)fragments.get(2)).getCovered(target.getName()));
		assertNotNull(((MessageOccurrenceSpecification)fragments.get(3)).getCovered(source.getName()));
	}

	/**
	 * Test createSynchronousMessage service. Create a synchronous message between an existing execution and
	 * an empty lifeline.
	 * 
	 * @throws Exception
	 *             in case of error
	 */
	public void testCreateSynchronousMessageOnExecution() throws Exception {
		Interaction interaction = getInteraction(RESOURCE_URI, "Scenario_3");
		Lifeline source = interaction.getLifeline("consumers");
		Lifeline target = interaction.getLifeline("producers");
		ExecutionSpecification execution = (ExecutionSpecification)interaction.getFragment("compute");

		sequenceServices.createSynchronousMessage(interaction, execution, target, execution.getStart(),
				execution.getStart());

		List<InteractionFragment> fragments = interaction.getFragments();
		assertEquals(8, fragments.size());
		InteractionFragment computeStart = fragments.get(0);
		InteractionFragment compute = fragments.get(1);
		InteractionFragment messageSend = fragments.get(2);
		InteractionFragment messageReceive = fragments.get(3);
		InteractionFragment messageReplySend = fragments.get(4);
		InteractionFragment messageReplyReceive = fragments.get(5);
		InteractionFragment compute2 = fragments.get(6);
		InteractionFragment computeFinish2 = fragments.get(7);

		// Execution compute
		Behavior computeBehavior = interaction.getOwnedBehaviors().get(0);
		assertEquals("Opaque behavior does not exist", "compute", computeBehavior.getName());
		assertTrue(computeStart instanceof ExecutionOccurrenceSpecification);
		assertEquals("compute_start", computeStart.getName());
		assertTrue(messageSend instanceof MessageOccurrenceSpecification);
		assertEquals("Message_0_sender", messageSend.getName());
		assertTrue(compute instanceof BehaviorExecutionSpecification);
		assertEquals("compute", compute.getName());
		assertEquals(((BehaviorExecutionSpecification)compute).getStart(), computeStart);
		assertEquals(((BehaviorExecutionSpecification)compute).getFinish(), messageSend);
		assertNotNull(((BehaviorExecutionSpecification)compute).getCovered(source.getName()));
		assertEquals(computeBehavior, ((BehaviorExecutionSpecification)compute).getBehavior());

		// Message Message_0
		Message message = interaction.getMessages().get(0);
		assertEquals("Message does not exist", "Message_0", message.getName());
		assertTrue(messageSend instanceof MessageOccurrenceSpecification);
		assertEquals("Message_0_sender", messageSend.getName());
		assertTrue(messageReceive instanceof MessageOccurrenceSpecification);
		assertEquals("Message_0_receiver", messageReceive.getName());
		assertEquals(message.getSendEvent(), messageSend);
		assertEquals(message.getReceiveEvent(), messageReceive);
		assertNotNull(((MessageOccurrenceSpecification)messageSend).getCovered(source.getName()));
		assertNotNull(((MessageOccurrenceSpecification)messageReceive).getCovered(target.getName()));

		// Reply message Message_0
		Message messageReply = interaction.getMessages().get(1);
		assertEquals("Message does not exist", "Message_0_reply", messageReply.getName());
		assertTrue(messageReplySend instanceof MessageOccurrenceSpecification);
		assertEquals("Message_0_reply_sender", messageReplySend.getName());
		assertTrue(messageReplyReceive instanceof MessageOccurrenceSpecification);
		assertEquals("Message_0_reply_receiver", messageReplyReceive.getName());
		assertEquals(messageReply.getSendEvent(), messageReplySend);
		assertEquals(messageReply.getReceiveEvent(), messageReplyReceive);
		assertNotNull(((MessageOccurrenceSpecification)messageReplySend).getCovered(target.getName()));
		assertNotNull(((MessageOccurrenceSpecification)messageReplyReceive).getCovered(source.getName()));

		// Execution compute split
		assertEquals("Opaque behavior does not exist", "compute", computeBehavior.getName());
		assertTrue(computeFinish2 instanceof ExecutionOccurrenceSpecification);
		assertEquals("compute_finish", computeFinish2.getName());
		assertTrue(compute2 instanceof BehaviorExecutionSpecification);
		assertEquals("compute", compute2.getName());
		assertEquals(((BehaviorExecutionSpecification)compute2).getStart(), messageReplyReceive);
		assertEquals(((BehaviorExecutionSpecification)compute2).getFinish(), computeFinish2);
		assertNotNull(((BehaviorExecutionSpecification)compute2).getCovered(source.getName()));
		assertEquals(computeBehavior, ((BehaviorExecutionSpecification)compute2).getBehavior());
	}

	/**
	 * Test createSynchronousMessage service. Create a synchronous message between an existing execution and
	 * an other execution.
	 * 
	 * @throws Exception
	 *             in case of error
	 */
	public void testCreateSynchronousMessageOnExecutionToExecution() throws Exception {
		// Interaction Scenario_4
		Interaction interaction = getInteraction(RESOURCE_URI, "Scenario_4");
		// SourceFragment compute execution
		ExecutionSpecification executionSource = (ExecutionSpecification)interaction.getFragment("compute");
		// TargetFragment get execution
		ExecutionSpecification executionTarget = (ExecutionSpecification)interaction.getFragment("get");
		// Operation null
		// StartingEndPredecessor compute_start
		// FinishingEndPredecessor compute_start
		Lifeline source = interaction.getLifeline("consumers");
		Lifeline target = interaction.getLifeline("producers");

		sequenceServices.createSynchronousMessage(interaction, executionSource, executionTarget,
				executionSource.getStart(), executionSource.getStart());

		List<InteractionFragment> fragments = interaction.getFragments();
		assertEquals(9, fragments.size());

		InteractionFragment computeStart = fragments.get(0);
		InteractionFragment compute = fragments.get(1);
		InteractionFragment getSend = fragments.get(2);
		InteractionFragment getReceive = fragments.get(3);
		InteractionFragment get = fragments.get(4);
		InteractionFragment getReplySend = fragments.get(5);
		InteractionFragment getReplyReceive = fragments.get(6);
		InteractionFragment compute2 = fragments.get(7);
		InteractionFragment computeFinish2 = fragments.get(8);

		// Execution compute
		Behavior computeBehavior = interaction.getOwnedBehaviors().get(0);
		assertEquals("Opaque behavior does not exist", "compute", computeBehavior.getName());
		assertTrue(computeStart instanceof ExecutionOccurrenceSpecification);
		assertEquals("compute_start", computeStart.getName());
		assertTrue(getSend instanceof MessageOccurrenceSpecification);
		assertEquals("get_sender", getSend.getName());
		assertTrue(compute instanceof BehaviorExecutionSpecification);
		assertEquals("compute", compute.getName());
		assertEquals(((BehaviorExecutionSpecification)compute).getStart(), computeStart);
		assertEquals(((BehaviorExecutionSpecification)compute).getFinish(), getSend);
		assertNotNull(((BehaviorExecutionSpecification)compute).getCovered(source.getName()));
		assertEquals(computeBehavior, ((BehaviorExecutionSpecification)compute).getBehavior());

		// Message get
		Message getMessage = interaction.getMessages().get(0);
		assertEquals("Message does not exist", "get", getMessage.getName());
		assertTrue(getSend instanceof MessageOccurrenceSpecification);
		assertEquals("get_sender", getSend.getName());
		assertTrue(getReceive instanceof MessageOccurrenceSpecification);
		assertEquals("get_receiver", getReceive.getName());
		assertEquals(getMessage.getSendEvent(), getSend);
		assertEquals(getMessage.getReceiveEvent(), getReceive);
		assertNotNull(((MessageOccurrenceSpecification)getSend).getCovered(source.getName()));
		assertNotNull(((MessageOccurrenceSpecification)getReceive).getCovered(target.getName()));

		// Execution get
		Behavior getBehavior = interaction.getOwnedBehaviors().get(1);
		assertEquals("Opaque behavior does not exist", "get", getBehavior.getName());
		assertTrue(getReceive instanceof MessageOccurrenceSpecification);
		assertEquals("get_receiver", getReceive.getName());
		assertTrue(getReplySend instanceof MessageOccurrenceSpecification);
		assertEquals("get_reply_sender", getReplySend.getName());
		assertTrue(get instanceof BehaviorExecutionSpecification);
		assertEquals("get", get.getName());
		assertEquals(((BehaviorExecutionSpecification)get).getStart(), getReceive);
		assertEquals(((BehaviorExecutionSpecification)get).getFinish(), getReplySend);
		assertNotNull(((BehaviorExecutionSpecification)get).getCovered(target.getName()));
		assertEquals(getBehavior, ((BehaviorExecutionSpecification)get).getBehavior());

		// Reply message get
		Message getReplyMessage = interaction.getMessages().get(1);
		assertEquals("Message does not exist", "get_reply", getReplyMessage.getName());
		assertTrue(getReplySend instanceof MessageOccurrenceSpecification);
		assertEquals("get_reply_sender", getReplySend.getName());
		assertTrue(getReplyReceive instanceof MessageOccurrenceSpecification);
		assertEquals("get_reply_receiver", getReplyReceive.getName());
		assertEquals(getReplyMessage.getSendEvent(), getReplySend);
		assertEquals(getReplyMessage.getReceiveEvent(), getReplyReceive);
		assertNotNull(((MessageOccurrenceSpecification)getReplySend).getCovered(target.getName()));
		assertNotNull(((MessageOccurrenceSpecification)getReplyReceive).getCovered(source.getName()));

		// Execution compute2
		assertEquals("Opaque behavior does not exist", "compute", computeBehavior.getName());
		assertTrue(computeFinish2 instanceof ExecutionOccurrenceSpecification);
		assertEquals("compute_finish", computeFinish2.getName());
		assertTrue(compute2 instanceof BehaviorExecutionSpecification);
		assertEquals("compute", compute2.getName());
		assertEquals(((BehaviorExecutionSpecification)compute2).getStart(), getReplyReceive);
		assertEquals(((BehaviorExecutionSpecification)compute2).getFinish(), computeFinish2);
		assertNotNull(((BehaviorExecutionSpecification)compute2).getCovered(source.getName()));
		assertEquals(computeBehavior, ((BehaviorExecutionSpecification)compute2).getBehavior());
	}

	/**
	 * Test createSynchronousMessage service. Create a synchronous message between an existing execution and
	 * an empty lifeline.
	 * 
	 * @throws Exception
	 *             in case of error
	 */
	public void testImportSynchronousMessageOnExecution() throws Exception {
		Interaction interaction = getInteraction(RESOURCE_URI, "Scenario_3");
		Lifeline source = interaction.getLifeline("consumers");
		Lifeline target = interaction.getLifeline("producers");
		Operation operation = (Operation)target.getRepresents().getType().getOwnedElements().get(1);
		ExecutionSpecification execution = (ExecutionSpecification)interaction.getFragment("compute");

		sequenceServices.createSynchronousMessage(interaction, execution, target, operation,
				execution.getStart(), execution.getStart());

		List<InteractionFragment> fragments = interaction.getFragments();
		assertEquals(9, fragments.size());

		InteractionFragment computeStart = fragments.get(0);
		InteractionFragment compute = fragments.get(1);
		InteractionFragment getSend = fragments.get(2);
		InteractionFragment getReceive = fragments.get(3);
		InteractionFragment get = fragments.get(4);
		InteractionFragment getReplySend = fragments.get(5);
		InteractionFragment getReplyReceive = fragments.get(6);
		InteractionFragment compute2 = fragments.get(7);
		InteractionFragment computeFinish2 = fragments.get(8);

		// Execution compute
		Behavior computeBehavior = interaction.getOwnedBehaviors().get(0);
		assertEquals("Opaque behavior does not exist", "compute", computeBehavior.getName());
		assertTrue(computeStart instanceof ExecutionOccurrenceSpecification);
		assertEquals("compute_start", computeStart.getName());
		assertTrue(getSend instanceof MessageOccurrenceSpecification);
		assertEquals("get_sender", getSend.getName());
		assertTrue(compute instanceof BehaviorExecutionSpecification);
		assertEquals("compute", compute.getName());
		assertEquals(((BehaviorExecutionSpecification)compute).getStart(), computeStart);
		assertEquals(((BehaviorExecutionSpecification)compute).getFinish(), getSend);
		assertNotNull(((BehaviorExecutionSpecification)compute).getCovered(source.getName()));
		assertEquals(computeBehavior, ((BehaviorExecutionSpecification)compute).getBehavior());

		// Message get
		Message getMessage = interaction.getMessages().get(0);
		assertEquals("Message does not exist", "get", getMessage.getName());
		assertTrue(getSend instanceof MessageOccurrenceSpecification);
		assertEquals("get_sender", getSend.getName());
		assertTrue(getReceive instanceof MessageOccurrenceSpecification);
		assertEquals("get_receiver", getReceive.getName());
		assertEquals(getMessage.getSendEvent(), getSend);
		assertEquals(getMessage.getReceiveEvent(), getReceive);
		assertNotNull(((MessageOccurrenceSpecification)getSend).getCovered(source.getName()));
		assertNotNull(((MessageOccurrenceSpecification)getReceive).getCovered(target.getName()));

		// Execution get
		Behavior getBehavior = interaction.getOwnedBehaviors().get(1);
		assertEquals("Opaque behavior does not exist", "get", getBehavior.getName());
		assertTrue(get instanceof BehaviorExecutionSpecification);
		assertEquals("get", get.getName());
		assertEquals(((BehaviorExecutionSpecification)get).getStart(), getReceive);
		assertEquals(((BehaviorExecutionSpecification)get).getFinish(), getReplySend);
		assertNotNull(((BehaviorExecutionSpecification)get).getCovered(target.getName()));
		assertEquals(getBehavior, ((BehaviorExecutionSpecification)get).getBehavior());

		// Reply message get
		Message getReplyMessage = interaction.getMessages().get(1);
		assertEquals("Message does not exist", "get_reply", getReplyMessage.getName());
		assertTrue(getReplySend instanceof MessageOccurrenceSpecification);
		assertEquals("get_reply_sender", getReplySend.getName());
		assertTrue(getReplyReceive instanceof MessageOccurrenceSpecification);
		assertEquals("get_reply_receiver", getReplyReceive.getName());
		assertEquals(getReplyMessage.getSendEvent(), getReplySend);
		assertEquals(getReplyMessage.getReceiveEvent(), getReplyReceive);
		assertNotNull(((MessageOccurrenceSpecification)getReplySend).getCovered(target.getName()));
		assertNotNull(((MessageOccurrenceSpecification)getReplyReceive).getCovered(source.getName()));

		assertTrue(computeFinish2 instanceof ExecutionOccurrenceSpecification);
		assertEquals("compute_finish", computeFinish2.getName());

		// Execution compute split
		assertTrue(compute2 instanceof BehaviorExecutionSpecification);
		assertEquals("compute", compute2.getName());
		assertEquals(((BehaviorExecutionSpecification)compute2).getStart(), getReplyReceive);
		assertEquals(((BehaviorExecutionSpecification)compute2).getFinish(), computeFinish2);
		assertNotNull(((BehaviorExecutionSpecification)compute2).getCovered(source.getName()));
		assertEquals(computeBehavior, ((BehaviorExecutionSpecification)compute2).getBehavior());
	}

	/**
	 * Test createSynchronousMessage service. Import a synchronous message between to empty lifelines.
	 * 
	 * @throws Exception
	 *             in case of error
	 */
	public void testImportSynchronousMessage() throws Exception {
		Interaction interaction = getInteraction(RESOURCE_URI, "Scenario_2");
		Lifeline source = interaction.getLifeline("consumers");
		Lifeline target = interaction.getLifeline("producers");
		Operation operation = (Operation)source.getRepresents().getType().getOwnedElements().get(0);

		sequenceServices.createSynchronousMessage(interaction, source, target, operation, null, null);

		List<InteractionFragment> fragments = interaction.getFragments();
		assertEquals(5, fragments.size());
		// Message
		assertEquals("Message does not exist", "compute", interaction.getMessages().get(0).getName());
		assertTrue(fragments.get(0) instanceof MessageOccurrenceSpecification);
		assertEquals("compute_sender", fragments.get(0).getName());
		assertTrue(fragments.get(1) instanceof MessageOccurrenceSpecification);
		assertEquals("compute_receiver", fragments.get(1).getName());
		assertEquals(((Message)interaction.getMessages().get(0)).getSendEvent(), fragments.get(0));
		assertEquals(((Message)interaction.getMessages().get(0)).getReceiveEvent(), fragments.get(1));
		assertNotNull(((MessageOccurrenceSpecification)fragments.get(0)).getCovered(source.getName()));
		assertNotNull(((MessageOccurrenceSpecification)fragments.get(1)).getCovered(target.getName()));

		// Reply message
		assertEquals("Message does not exist", "compute_reply", interaction.getMessages().get(1).getName());
		assertTrue(fragments.get(3) instanceof MessageOccurrenceSpecification);
		assertEquals("compute_reply_sender", fragments.get(3).getName());
		assertTrue(fragments.get(4) instanceof MessageOccurrenceSpecification);
		assertEquals("compute_reply_receiver", fragments.get(4).getName());
		assertEquals(((Message)interaction.getMessages().get(1)).getSendEvent(), fragments.get(3));
		assertEquals(((Message)interaction.getMessages().get(1)).getReceiveEvent(), fragments.get(4));
		assertNotNull(((MessageOccurrenceSpecification)fragments.get(3)).getCovered(target.getName()));
		assertNotNull(((MessageOccurrenceSpecification)fragments.get(4)).getCovered(source.getName()));

		// Execution
		assertEquals("Opaque behavior does not exist", "compute", interaction.getOwnedBehaviors().get(0)
				.getName());
		assertTrue(fragments.get(2) instanceof BehaviorExecutionSpecification);
		assertEquals("compute", fragments.get(2).getName());
		assertEquals(((BehaviorExecutionSpecification)fragments.get(2)).getStart(), fragments.get(1));
		assertEquals(((BehaviorExecutionSpecification)fragments.get(2)).getFinish(), fragments.get(3));
		assertNotNull(((BehaviorExecutionSpecification)fragments.get(2)).getCovered(target.getName()));
		assertEquals(interaction.getOwnedBehaviors().get(0),
				((BehaviorExecutionSpecification)fragments.get(2)).getBehavior());
	}

	/**
	 * Test createSynchronousMessage service. Create a synchronous message between an existing execution and a
	 * lifeline. An execution exists on the source which already defines an asynchronous message.
	 * 
	 * @throws Exception
	 *             in case of error
	 */
	public void testImportSynchronousMessageOnExecution2() throws Exception {
		// Interaction Scenario_8
		Interaction interaction = getInteraction(RESOURCE_URI, "Scenario_8");
		Lifeline source = interaction.getLifeline("consumers");
		// SourceFragment compute execution
		ExecutionSpecification execution = (ExecutionSpecification)interaction.getFragment("compute");
		// TargetFragment producers lifeline
		Lifeline target = interaction.getLifeline("producers");
		// Operation produce
		Operation operation = (Operation)target.getRepresents().getType().getOwnedElements().get(0);
		// StratingEndPredecessor get_finish execution occurrence
		// FinishindEnPredecessor get_finish execution occurrence
		ExecutionSpecification execution2 = (ExecutionSpecification)interaction.getFragment("get");

		sequenceServices.createSynchronousMessage(interaction, execution, target, operation,
				execution2.getFinish(), execution2.getFinish());

		List<InteractionFragment> fragments = interaction.getFragments();
		assertEquals(13, fragments.size());

		InteractionFragment computeStart = fragments.get(0);
		InteractionFragment compute = fragments.get(1);
		InteractionFragment getSend = fragments.get(2);
		InteractionFragment getReceive = fragments.get(3);
		InteractionFragment get = fragments.get(4);
		InteractionFragment getFinish = fragments.get(5);
		InteractionFragment produceSend = fragments.get(6);
		InteractionFragment produceReceive = fragments.get(7);
		InteractionFragment produce = fragments.get(8);
		InteractionFragment produceReplySend = fragments.get(9);
		InteractionFragment produceReplyReceive = fragments.get(10);
		InteractionFragment compute2 = fragments.get(11);
		InteractionFragment computeFinish2 = fragments.get(12);

		// Execution compute
		Behavior computeBehavior = interaction.getOwnedBehaviors().get(0);
		assertEquals("Opaque behavior does not exist", "compute", computeBehavior.getName());
		assertTrue(computeStart instanceof ExecutionOccurrenceSpecification);
		assertEquals("compute_start", computeStart.getName());
		assertTrue(produceSend instanceof MessageOccurrenceSpecification);
		assertEquals("produce_sender", produceSend.getName());
		assertTrue(compute instanceof BehaviorExecutionSpecification);
		assertEquals("compute", compute.getName());
		assertEquals(((BehaviorExecutionSpecification)compute).getStart(), computeStart);
		assertEquals(((BehaviorExecutionSpecification)compute).getFinish(), produceSend);
		assertNotNull(((BehaviorExecutionSpecification)compute).getCovered(source.getName()));
		assertEquals(computeBehavior, ((BehaviorExecutionSpecification)compute).getBehavior());

		// Asynchronous message get
		Message messageGet = interaction.getMessages().get(0);
		assertEquals("Message does not exist", "get", messageGet.getName());
		assertTrue(getSend instanceof MessageOccurrenceSpecification);
		assertEquals("get_sender", getSend.getName());
		assertTrue(getReceive instanceof MessageOccurrenceSpecification);
		assertEquals("get_receiver", getReceive.getName());
		assertEquals(messageGet.getSendEvent(), getSend);
		assertEquals(messageGet.getReceiveEvent(), getReceive);
		assertNotNull(((MessageOccurrenceSpecification)getSend).getCovered(source.getName()));
		assertNotNull(((MessageOccurrenceSpecification)getReceive).getCovered(target.getName()));

		// Execution get
		Behavior getBehavior = interaction.getOwnedBehaviors().get(1);
		assertEquals("Opaque behavior does not exist", "get", getBehavior.getName());
		assertTrue(getReceive instanceof MessageOccurrenceSpecification);
		assertEquals("get_receiver", getReceive.getName());
		assertTrue(getFinish instanceof ExecutionOccurrenceSpecification);
		assertEquals("get_finish", getFinish.getName());
		assertTrue(get instanceof BehaviorExecutionSpecification);
		assertEquals("get", get.getName());
		assertEquals(((BehaviorExecutionSpecification)get).getStart(), getReceive);
		assertEquals(((BehaviorExecutionSpecification)get).getFinish(), getFinish);
		assertNotNull(((BehaviorExecutionSpecification)get).getCovered(target.getName()));
		assertEquals(getBehavior, ((BehaviorExecutionSpecification)get).getBehavior());

		// Synchronous message produce
		Message messageProduce = interaction.getMessages().get(1);
		assertEquals("Message does not exist", "produce", messageProduce.getName());
		assertTrue(produceSend instanceof MessageOccurrenceSpecification);
		assertEquals("produce_sender", produceSend.getName());
		assertTrue(produceReceive instanceof MessageOccurrenceSpecification);
		assertEquals("produce_receiver", produceReceive.getName());
		assertEquals(messageProduce.getSendEvent(), produceSend);
		assertEquals(messageProduce.getReceiveEvent(), produceReceive);
		assertNotNull(((MessageOccurrenceSpecification)produceSend).getCovered(source.getName()));
		assertNotNull(((MessageOccurrenceSpecification)produceReceive).getCovered(target.getName()));

		// Execution produce
		Behavior produceBehavior = interaction.getOwnedBehaviors().get(2);
		assertEquals("Opaque behavior does not exist", "produce", produceBehavior.getName());
		assertTrue(produceReceive instanceof MessageOccurrenceSpecification);
		assertEquals("produce_receiver", produceReceive.getName());
		assertTrue(produceReplySend instanceof MessageOccurrenceSpecification);
		assertEquals("produce_reply_sender", produceReplySend.getName());
		assertTrue(produce instanceof BehaviorExecutionSpecification);
		assertEquals("produce", produce.getName());
		assertEquals(((BehaviorExecutionSpecification)produce).getStart(), produceReceive);
		assertEquals(((BehaviorExecutionSpecification)produce).getFinish(), produceReplySend);
		assertNotNull(((BehaviorExecutionSpecification)produce).getCovered(target.getName()));
		assertEquals(produceBehavior, ((BehaviorExecutionSpecification)produce).getBehavior());

		// Synchronous message reply produce
		Message messageReplyProduce = interaction.getMessages().get(2);
		assertEquals("Message does not exist", "produce_reply", messageReplyProduce.getName());
		assertTrue(produceReplySend instanceof MessageOccurrenceSpecification);
		assertEquals("produce_reply_sender", produceReplySend.getName());
		assertTrue(produceReplyReceive instanceof MessageOccurrenceSpecification);
		assertEquals("produce_reply_receiver", produceReplyReceive.getName());
		assertEquals(messageReplyProduce.getSendEvent(), produceReplySend);
		assertEquals(messageReplyProduce.getReceiveEvent(), produceReplyReceive);
		assertNotNull(((MessageOccurrenceSpecification)produceReplySend).getCovered(target.getName()));
		assertNotNull(((MessageOccurrenceSpecification)produceReplyReceive).getCovered(source.getName()));

		// Execution compute2
		assertTrue(produceReplyReceive instanceof MessageOccurrenceSpecification);
		assertEquals("produce_reply_receiver", produceReplyReceive.getName());
		assertTrue(computeFinish2 instanceof ExecutionOccurrenceSpecification);
		assertEquals("compute_finish", computeFinish2.getName());
		assertTrue(compute2 instanceof BehaviorExecutionSpecification);
		assertEquals("compute", compute.getName());
		assertEquals(((BehaviorExecutionSpecification)compute2).getStart(), produceReplyReceive);
		assertEquals(((BehaviorExecutionSpecification)compute2).getFinish(), computeFinish2);
		assertNotNull(((BehaviorExecutionSpecification)compute2).getCovered(source.getName()));
		assertEquals(computeBehavior, ((BehaviorExecutionSpecification)compute2).getBehavior());
	}

	/**
	 * Test createSynchronousMessage service. Create a synchronous message between an existing execution and a
	 * lifeline. An execution exists on the source which already defines an asynchronous message.
	 * 
	 * @throws Exception
	 *             in case of error
	 */
	public void testImportAsynchronousMessageOnExecution2() throws Exception {
		// Interaction Scenario_9
		Interaction interaction = getInteraction(RESOURCE_URI, "Scenario_9");
		Lifeline source = interaction.getLifeline("consumers");
		// SourceFragment compute execution
		ExecutionSpecification execution = (ExecutionSpecification)interaction.getFragment("compute");
		// TargetFragment producers lifeline
		Lifeline target = interaction.getLifeline("producers");
		// Operation produce
		Operation operation = (Operation)target.getRepresents().getType().getOwnedElements().get(0);
		// StratingEndPredecessor get_reply_sender execution occurrence
		// FinishindEnPredecessor get_reply_sender execution occurrence
		ExecutionSpecification execution2 = (ExecutionSpecification)interaction.getFragment("get");

		sequenceServices.createSynchronousMessage(interaction, execution, target, operation,
				execution2.getFinish(), execution2.getFinish());

		List<InteractionFragment> fragments = interaction.getFragments();
		assertEquals(13, fragments.size());

		InteractionFragment computeStart = fragments.get(0);
		InteractionFragment compute = fragments.get(1);
		InteractionFragment getSend = fragments.get(2);
		InteractionFragment getReceive = fragments.get(3);
		InteractionFragment get = fragments.get(4);
		InteractionFragment getReplySend = fragments.get(5);
		InteractionFragment getReplyReceive = fragments.get(6);
		InteractionFragment compute2 = fragments.get(7);
		InteractionFragment produceSend = fragments.get(8);
		InteractionFragment produceReceive = fragments.get(9);
		InteractionFragment produce = fragments.get(10);
		InteractionFragment produceFinish = fragments.get(11);
		InteractionFragment computeFinish2 = fragments.get(12);

		// Execution compute
		Behavior computeBehavior = interaction.getOwnedBehaviors().get(0);
		assertEquals("Opaque behavior does not exist", "compute", computeBehavior.getName());
		assertTrue(computeStart instanceof ExecutionOccurrenceSpecification);
		assertEquals("compute_start", computeStart.getName());
		assertTrue(produceSend instanceof MessageOccurrenceSpecification);
		assertEquals("produce_sender", produceSend.getName());
		assertTrue(compute instanceof BehaviorExecutionSpecification);
		assertEquals("compute", compute.getName());
		assertEquals(((BehaviorExecutionSpecification)compute).getStart(), computeStart);
		assertEquals(((BehaviorExecutionSpecification)compute).getFinish(), produceSend);
		assertNotNull(((BehaviorExecutionSpecification)compute).getCovered(source.getName()));
		assertEquals(computeBehavior, ((BehaviorExecutionSpecification)compute).getBehavior());

		// Synchronous message get
		Message messageGet = interaction.getMessages().get(0);
		assertEquals("Message does not exist", "get", messageGet.getName());
		assertTrue(getSend instanceof MessageOccurrenceSpecification);
		assertEquals("get_sender", getSend.getName());
		assertTrue(getReceive instanceof MessageOccurrenceSpecification);
		assertEquals("get_receiver", getReceive.getName());
		assertEquals(messageGet.getSendEvent(), getSend);
		assertEquals(messageGet.getReceiveEvent(), getReceive);
		assertNotNull(((MessageOccurrenceSpecification)getSend).getCovered(source.getName()));
		assertNotNull(((MessageOccurrenceSpecification)getReceive).getCovered(target.getName()));

		// Execution get
		Behavior getBehavior = interaction.getOwnedBehaviors().get(1);
		assertEquals("Opaque behavior does not exist", "get", getBehavior.getName());
		assertTrue(getReceive instanceof MessageOccurrenceSpecification);
		assertEquals("get_receiver", getReceive.getName());
		assertTrue(getReplySend instanceof MessageOccurrenceSpecification);
		assertEquals("get_reply_sender", getReplySend.getName());
		assertTrue(get instanceof BehaviorExecutionSpecification);
		assertEquals("get", get.getName());
		assertEquals(((BehaviorExecutionSpecification)get).getStart(), getReceive);
		assertEquals(((BehaviorExecutionSpecification)get).getFinish(), getReplySend);
		assertNotNull(((BehaviorExecutionSpecification)get).getCovered(target.getName()));
		assertEquals(getBehavior, ((BehaviorExecutionSpecification)get).getBehavior());

		// Synchronous message reply get
		Message messageReplyGet = interaction.getMessages().get(2);
		assertEquals("Message does not exist", "get_reply", messageReplyGet.getName());
		assertTrue(getReplySend instanceof MessageOccurrenceSpecification);
		assertEquals("get_reply_sender", getReplySend.getName());
		assertTrue(getReplyReceive instanceof MessageOccurrenceSpecification);
		assertEquals("get_reply_receiver", getReplyReceive.getName());
		assertEquals(messageReplyGet.getSendEvent(), getReplySend);
		assertEquals(messageReplyGet.getReceiveEvent(), getReplyReceive);
		assertNotNull(((MessageOccurrenceSpecification)getReplySend).getCovered(target.getName()));
		assertNotNull(((MessageOccurrenceSpecification)getReplyReceive).getCovered(source.getName()));

		// Execution compute2
		assertTrue(getReplyReceive instanceof MessageOccurrenceSpecification);
		assertEquals("get_reply_receiver", getReplyReceive.getName());
		assertTrue(computeFinish2 instanceof ExecutionOccurrenceSpecification);
		assertEquals("compute_finish", computeFinish2.getName());
		assertTrue(compute2 instanceof BehaviorExecutionSpecification);
		assertEquals("compute", compute.getName());
		assertEquals(((BehaviorExecutionSpecification)compute2).getStart(), getReplyReceive);
		assertEquals(((BehaviorExecutionSpecification)compute2).getFinish(), computeFinish2);
		assertNotNull(((BehaviorExecutionSpecification)compute2).getCovered(source.getName()));
		assertEquals(computeBehavior, ((BehaviorExecutionSpecification)compute2).getBehavior());

		// Asynchronous message produce
		Message messageProduce = interaction.getMessages().get(1);
		assertEquals("Message does not exist", "produce", messageProduce.getName());
		assertTrue(produceSend instanceof MessageOccurrenceSpecification);
		assertEquals("produce_sender", produceSend.getName());
		assertTrue(produceReceive instanceof MessageOccurrenceSpecification);
		assertEquals("produce_receiver", produceReceive.getName());
		assertEquals(messageProduce.getSendEvent(), produceSend);
		assertEquals(messageProduce.getReceiveEvent(), produceReceive);
		assertNotNull(((MessageOccurrenceSpecification)produceSend).getCovered(source.getName()));
		assertNotNull(((MessageOccurrenceSpecification)produceReceive).getCovered(target.getName()));

		// Execution produce
		Behavior produceBehavior = interaction.getOwnedBehaviors().get(2);
		assertEquals("Opaque behavior does not exist", "produce", produceBehavior.getName());
		assertTrue(produceReceive instanceof MessageOccurrenceSpecification);
		assertEquals("produce_receiver", produceReceive.getName());
		assertTrue(produceFinish instanceof ExecutionOccurrenceSpecification);
		assertEquals("produce_finish", produceFinish.getName());
		assertTrue(produce instanceof BehaviorExecutionSpecification);
		assertEquals("produce", produce.getName());
		assertEquals(((BehaviorExecutionSpecification)produce).getStart(), produceReceive);
		assertEquals(((BehaviorExecutionSpecification)produce).getFinish(), produceFinish);
		assertNotNull(((BehaviorExecutionSpecification)produce).getCovered(target.getName()));
		assertEquals(produceBehavior, ((BehaviorExecutionSpecification)produce).getBehavior());
	}
}
