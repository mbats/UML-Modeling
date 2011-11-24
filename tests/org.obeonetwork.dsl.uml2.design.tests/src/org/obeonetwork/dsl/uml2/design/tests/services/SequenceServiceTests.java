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
	private static final String RESOURCE_URI2 = Activator.PLUGIN_ID + "/resources/Test2.uml";

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
		Interaction interaction = getInteraction(RESOURCE_URI2, "Scenario_5");
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
		Interaction interaction = getInteraction(RESOURCE_URI2, "Scenario_5");
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
		Interaction interaction = getInteraction(RESOURCE_URI2, "Scenario_6");
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
		Interaction interaction = getInteraction(RESOURCE_URI2, "Scenario_0");
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
		Interaction interaction = getInteraction(RESOURCE_URI2, "Scenario_0");
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
		Interaction interaction = getInteraction(RESOURCE_URI2, "Scenario_1");
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
		Interaction interaction = getInteraction(RESOURCE_URI2, "Scenario_2");
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
		Interaction interaction = getInteraction(RESOURCE_URI2, "Scenario_3");
		Lifeline source = interaction.getLifeline("consumers");
		Lifeline target = interaction.getLifeline("producers");
		ExecutionSpecification execution = (ExecutionSpecification)interaction.getFragment("compute");

		sequenceServices.createAsynchronousMessage(interaction, execution, target, execution.getStart(),
				execution.getStart());

		List<InteractionFragment> fragments = interaction.getFragments();
		assertEquals(5, fragments.size());
		// Execution
		assertEquals("Opaque behavior does not exist", "compute", interaction.getOwnedBehaviors().get(0)
				.getName());
		assertTrue(fragments.get(0) instanceof ExecutionOccurrenceSpecification);
		assertEquals("compute_start", fragments.get(0).getName());
		assertTrue(fragments.get(3) instanceof ExecutionOccurrenceSpecification);
		assertEquals("compute_finish", fragments.get(3).getName());
		assertTrue(fragments.get(1) instanceof BehaviorExecutionSpecification);
		assertEquals("compute", fragments.get(1).getName());
		assertEquals(((BehaviorExecutionSpecification)fragments.get(1)).getStart(), fragments.get(0));
		assertEquals(((BehaviorExecutionSpecification)fragments.get(1)).getFinish(), fragments.get(3));
		assertNotNull(((BehaviorExecutionSpecification)fragments.get(1)).getCovered(source.getName()));
		assertEquals(interaction.getOwnedBehaviors().get(0),
				((BehaviorExecutionSpecification)fragments.get(1)).getBehavior());

		// Message
		assertEquals("Message does not exist", "Message_0", interaction.getMessages().get(0).getName());
		assertTrue(fragments.get(2) instanceof MessageOccurrenceSpecification);
		assertEquals("Message_0_sender", fragments.get(2).getName());
		assertTrue(fragments.get(4) instanceof MessageOccurrenceSpecification);
		assertEquals("Message_0_receiver", fragments.get(4).getName());
		assertEquals(((Message)interaction.getMessages().get(0)).getSendEvent(), fragments.get(2));
		assertEquals(((Message)interaction.getMessages().get(0)).getReceiveEvent(), fragments.get(4));
		assertNotNull(((MessageOccurrenceSpecification)fragments.get(2)).getCovered(source.getName()));
		assertNotNull(((MessageOccurrenceSpecification)fragments.get(4)).getCovered(target.getName()));
	}

	/**
	 * Test createAsynchronousMessage service. Import an asynchronous message between to empty lifelines.
	 * 
	 * @throws Exception
	 *             in case of error
	 */
	public void testImportAsynchronousMessage() throws Exception {
		Interaction interaction = getInteraction(RESOURCE_URI2, "Scenario_2");
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
		Interaction interaction = getInteraction(RESOURCE_URI2, "Scenario_3");
		Lifeline source = interaction.getLifeline("consumers");
		Lifeline target = interaction.getLifeline("producers");
		Operation operation = (Operation)target.getRepresents().getType().getOwnedElements().get(1);
		ExecutionSpecification execution = (ExecutionSpecification)interaction.getFragment("compute");

		sequenceServices.createAsynchronousMessage(interaction, execution, target, operation,
				execution.getStart(), execution.getStart());

		List<InteractionFragment> fragments = interaction.getFragments();
		assertEquals(7, fragments.size());
		// Execution compute
		assertEquals("Opaque behavior does not exist", "compute", interaction.getOwnedBehaviors().get(0)
				.getName());
		assertTrue(fragments.get(0) instanceof ExecutionOccurrenceSpecification);
		assertEquals("compute_start", fragments.get(0).getName());
		assertTrue(fragments.get(5) instanceof ExecutionOccurrenceSpecification);
		assertEquals("compute_finish", fragments.get(5).getName());
		assertTrue(fragments.get(1) instanceof BehaviorExecutionSpecification);
		assertEquals("compute", fragments.get(1).getName());
		assertEquals(((BehaviorExecutionSpecification)fragments.get(1)).getStart(), fragments.get(0));
		assertEquals(((BehaviorExecutionSpecification)fragments.get(1)).getFinish(), fragments.get(5));
		assertNotNull(((BehaviorExecutionSpecification)fragments.get(1)).getCovered(source.getName()));
		assertEquals(interaction.getOwnedBehaviors().get(0),
				((BehaviorExecutionSpecification)fragments.get(1)).getBehavior());

		// Message get
		assertEquals("Message does not exist", "get", interaction.getMessages().get(0).getName());
		assertTrue(fragments.get(2) instanceof MessageOccurrenceSpecification);
		assertEquals("get_sender", fragments.get(2).getName());
		assertTrue(fragments.get(3) instanceof MessageOccurrenceSpecification);
		assertEquals("get_receiver", fragments.get(3).getName());
		assertEquals(((Message)interaction.getMessages().get(0)).getSendEvent(), fragments.get(2));
		assertEquals(((Message)interaction.getMessages().get(0)).getReceiveEvent(), fragments.get(3));
		assertNotNull(((MessageOccurrenceSpecification)fragments.get(2)).getCovered(source.getName()));
		assertNotNull(((MessageOccurrenceSpecification)fragments.get(3)).getCovered(target.getName()));

		// Execution get
		assertEquals("Opaque behavior does not exist", "get", interaction.getOwnedBehaviors().get(1)
				.getName());
		assertTrue(fragments.get(4) instanceof BehaviorExecutionSpecification);
		assertEquals("get", fragments.get(4).getName());
		assertEquals(((BehaviorExecutionSpecification)fragments.get(4)).getStart(), fragments.get(3));
		assertTrue(fragments.get(6) instanceof ExecutionOccurrenceSpecification);
		assertEquals("get_finish", fragments.get(6).getName());
		assertEquals(((BehaviorExecutionSpecification)fragments.get(4)).getFinish(), fragments.get(6));
		assertNotNull(((BehaviorExecutionSpecification)fragments.get(4)).getCovered(target.getName()));
		assertEquals(interaction.getOwnedBehaviors().get(1),
				((BehaviorExecutionSpecification)fragments.get(4)).getBehavior());
	}

	/**
	 * Test createAsynchronousMessage service. Import an asynchronous message between an existing execution
	 * and an empty lifeline.
	 * 
	 * @throws Exception
	 *             in case of error
	 */
	public void testCreateAsynchronousMessageOnExecutionToExecution() throws Exception {
		Interaction interaction = getInteraction(RESOURCE_URI2, "Scenario_4");
		Lifeline source = interaction.getLifeline("consumers");
		Lifeline target = interaction.getLifeline("producers");
		ExecutionSpecification executionSource = (ExecutionSpecification)interaction.getFragment("compute");
		ExecutionSpecification executionTarget = (ExecutionSpecification)interaction.getFragment("get");
		sequenceServices.createAsynchronousMessage(interaction, executionSource, executionTarget,
				executionTarget.getStart(), executionTarget.getStart());

		List<InteractionFragment> fragments = interaction.getFragments();
		assertEquals(7, fragments.size());
		// Execution compute
		assertEquals("Opaque behavior does not exist", "compute", interaction.getOwnedBehaviors().get(0)
				.getName());
		assertTrue(fragments.get(0) instanceof ExecutionOccurrenceSpecification);
		assertEquals("compute_start", fragments.get(0).getName());
		assertTrue(fragments.get(5) instanceof ExecutionOccurrenceSpecification);
		assertEquals("compute_finish", fragments.get(5).getName());
		assertTrue(fragments.get(1) instanceof BehaviorExecutionSpecification);
		assertEquals("compute", fragments.get(1).getName());
		assertEquals(((BehaviorExecutionSpecification)fragments.get(1)).getStart(), fragments.get(0));
		assertEquals(((BehaviorExecutionSpecification)fragments.get(1)).getFinish(), fragments.get(5));
		assertNotNull(((BehaviorExecutionSpecification)fragments.get(1)).getCovered(source.getName()));
		assertEquals(interaction.getOwnedBehaviors().get(0),
				((BehaviorExecutionSpecification)fragments.get(1)).getBehavior());

		// Message get
		assertEquals("Message does not exist", "get", interaction.getMessages().get(0).getName());
		assertTrue(fragments.get(2) instanceof MessageOccurrenceSpecification);
		assertEquals("get_sender", fragments.get(2).getName());
		assertTrue(fragments.get(3) instanceof MessageOccurrenceSpecification);
		assertEquals("get_receiver", fragments.get(3).getName());
		assertEquals(((Message)interaction.getMessages().get(0)).getSendEvent(), fragments.get(2));
		assertEquals(((Message)interaction.getMessages().get(0)).getReceiveEvent(), fragments.get(3));
		assertNotNull(((MessageOccurrenceSpecification)fragments.get(2)).getCovered(source.getName()));
		assertNotNull(((MessageOccurrenceSpecification)fragments.get(3)).getCovered(target.getName()));

		// Execution get
		assertEquals("Opaque behavior does not exist", "get", interaction.getOwnedBehaviors().get(1)
				.getName());
		assertTrue(fragments.get(4) instanceof BehaviorExecutionSpecification);
		assertEquals("get", fragments.get(4).getName());
		assertEquals(((BehaviorExecutionSpecification)fragments.get(4)).getStart(), fragments.get(3));
		assertTrue(fragments.get(6) instanceof ExecutionOccurrenceSpecification);
		assertEquals("get_finish", fragments.get(6).getName());
		assertEquals(((BehaviorExecutionSpecification)fragments.get(4)).getFinish(), fragments.get(6));
		assertNotNull(((BehaviorExecutionSpecification)fragments.get(4)).getCovered(target.getName()));
		assertEquals(interaction.getOwnedBehaviors().get(1),
				((BehaviorExecutionSpecification)fragments.get(4)).getBehavior());
	}

	/**
	 * Test createSynchronousMessage service. Create a synchronous message between to empty lifelines.
	 * 
	 * @throws Exception
	 *             in case of error
	 */
	public void testCreateSynchronousMessage() throws Exception {
		Interaction interaction = getInteraction(RESOURCE_URI2, "Scenario_2");
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
		Interaction interaction = getInteraction(RESOURCE_URI2, "Scenario_3");
		Lifeline source = interaction.getLifeline("consumers");
		Lifeline target = interaction.getLifeline("producers");
		ExecutionSpecification execution = (ExecutionSpecification)interaction.getFragment("compute");

		sequenceServices.createSynchronousMessage(interaction, execution, target, execution.getStart(),
				execution.getStart());

		List<InteractionFragment> fragments = interaction.getFragments();
		assertEquals(9, fragments.size());
		// Execution
		assertEquals("Opaque behavior does not exist", "compute", interaction.getOwnedBehaviors().get(0)
				.getName());
		assertTrue(fragments.get(0) instanceof ExecutionOccurrenceSpecification);
		assertEquals("compute_start", fragments.get(0).getName());
		assertTrue(fragments.get(3) instanceof ExecutionOccurrenceSpecification);
		assertEquals("compute_finish", fragments.get(3).getName());
		assertTrue(fragments.get(1) instanceof BehaviorExecutionSpecification);
		assertEquals("compute", fragments.get(1).getName());
		assertEquals(((BehaviorExecutionSpecification)fragments.get(1)).getStart(), fragments.get(0));
		assertEquals(((BehaviorExecutionSpecification)fragments.get(1)).getFinish(), fragments.get(3));
		assertNotNull(((BehaviorExecutionSpecification)fragments.get(1)).getCovered(source.getName()));
		assertEquals(interaction.getOwnedBehaviors().get(0),
				((BehaviorExecutionSpecification)fragments.get(1)).getBehavior());

		// Message
		assertEquals("Message does not exist", "Message_0", interaction.getMessages().get(0).getName());
		assertTrue(fragments.get(2) instanceof MessageOccurrenceSpecification);
		assertEquals("Message_0_sender", fragments.get(2).getName());
		assertTrue(fragments.get(4) instanceof MessageOccurrenceSpecification);
		assertEquals("Message_0_receiver", fragments.get(4).getName());
		assertEquals(((Message)interaction.getMessages().get(0)).getSendEvent(), fragments.get(2));
		assertEquals(((Message)interaction.getMessages().get(0)).getReceiveEvent(), fragments.get(4));
		assertNotNull(((MessageOccurrenceSpecification)fragments.get(2)).getCovered(source.getName()));
		assertNotNull(((MessageOccurrenceSpecification)fragments.get(4)).getCovered(target.getName()));

		// Reply message
		assertEquals("Message does not exist", "Message_0_reply", interaction.getMessages().get(1).getName());
		assertTrue(fragments.get(5) instanceof MessageOccurrenceSpecification);
		assertEquals("Message_0_reply_sender", fragments.get(5).getName());
		assertTrue(fragments.get(6) instanceof MessageOccurrenceSpecification);
		assertEquals("Message_0_reply_receiver", fragments.get(6).getName());
		assertEquals(((Message)interaction.getMessages().get(1)).getSendEvent(), fragments.get(5));
		assertEquals(((Message)interaction.getMessages().get(1)).getReceiveEvent(), fragments.get(6));
		assertNotNull(((MessageOccurrenceSpecification)fragments.get(5)).getCovered(target.getName()));
		assertNotNull(((MessageOccurrenceSpecification)fragments.get(6)).getCovered(source.getName()));

		// Execution
		assertEquals("Opaque behavior does not exist", "compute", interaction.getOwnedBehaviors().get(0)
				.getName());
		assertTrue(fragments.get(8) instanceof ExecutionOccurrenceSpecification);
		assertEquals("compute_finish2", fragments.get(8).getName());
		assertTrue(fragments.get(7) instanceof BehaviorExecutionSpecification);
		assertEquals("compute", fragments.get(7).getName());
		assertEquals(((BehaviorExecutionSpecification)fragments.get(7)).getStart(), fragments.get(6));
		assertEquals(((BehaviorExecutionSpecification)fragments.get(7)).getFinish(), fragments.get(8));
		assertNotNull(((BehaviorExecutionSpecification)fragments.get(7)).getCovered(source.getName()));
		assertEquals(interaction.getOwnedBehaviors().get(0),
				((BehaviorExecutionSpecification)fragments.get(7)).getBehavior());
	}

	/**
	 * Test createSynchronousMessage service. Create a synchronous message between an existing execution and
	 * an other execution.
	 * 
	 * @throws Exception
	 *             in case of error
	 */
	public void testCreateSynchronousMessageOnExecutionToExecution() throws Exception {
		Interaction interaction = getInteraction(RESOURCE_URI2, "Scenario_4");
		Lifeline source = interaction.getLifeline("consumers");
		Lifeline target = interaction.getLifeline("producers");
		ExecutionSpecification executionSource = (ExecutionSpecification)interaction.getFragment("compute");
		ExecutionSpecification executionTarget = (ExecutionSpecification)interaction.getFragment("get");

		sequenceServices.createSynchronousMessage(interaction, executionSource, executionTarget,
				executionTarget.getStart(), executionTarget.getStart());

		List<InteractionFragment> fragments = interaction.getFragments();
		assertEquals(10, fragments.size());
		// Execution
		assertEquals("Opaque behavior does not exist", "compute", interaction.getOwnedBehaviors().get(0)
				.getName());
		assertTrue(fragments.get(0) instanceof ExecutionOccurrenceSpecification);
		assertEquals("compute_start", fragments.get(0).getName());
		assertTrue(fragments.get(5) instanceof ExecutionOccurrenceSpecification);
		assertEquals("compute_finish", fragments.get(5).getName());
		assertTrue(fragments.get(1) instanceof BehaviorExecutionSpecification);
		assertEquals("compute", fragments.get(1).getName());
		assertEquals(((BehaviorExecutionSpecification)fragments.get(1)).getStart(), fragments.get(0));
		assertEquals(((BehaviorExecutionSpecification)fragments.get(1)).getFinish(), fragments.get(5));
		assertNotNull(((BehaviorExecutionSpecification)fragments.get(1)).getCovered(source.getName()));
		assertEquals(interaction.getOwnedBehaviors().get(0),
				((BehaviorExecutionSpecification)fragments.get(1)).getBehavior());

		// Message
		assertEquals("Message does not exist", "get", interaction.getMessages().get(0).getName());
		assertTrue(fragments.get(2) instanceof MessageOccurrenceSpecification);
		assertEquals("get_sender", fragments.get(2).getName());
		assertTrue(fragments.get(3) instanceof MessageOccurrenceSpecification);
		assertEquals("get_receiver", fragments.get(3).getName());
		assertEquals(((Message)interaction.getMessages().get(0)).getSendEvent(), fragments.get(2));
		assertEquals(((Message)interaction.getMessages().get(0)).getReceiveEvent(), fragments.get(3));
		assertNotNull(((MessageOccurrenceSpecification)fragments.get(2)).getCovered(source.getName()));
		assertNotNull(((MessageOccurrenceSpecification)fragments.get(3)).getCovered(target.getName()));

		// Reply message
		assertEquals("Message does not exist", "get_reply", interaction.getMessages().get(1).getName());
		assertTrue(fragments.get(6) instanceof MessageOccurrenceSpecification);
		assertEquals("get_reply_sender", fragments.get(6).getName());
		assertTrue(fragments.get(7) instanceof MessageOccurrenceSpecification);
		assertEquals("get_reply_receiver", fragments.get(7).getName());
		assertEquals(((Message)interaction.getMessages().get(1)).getSendEvent(), fragments.get(6));
		assertEquals(((Message)interaction.getMessages().get(1)).getReceiveEvent(), fragments.get(7));
		assertNotNull(((MessageOccurrenceSpecification)fragments.get(6)).getCovered(target.getName()));
		assertNotNull(((MessageOccurrenceSpecification)fragments.get(7)).getCovered(source.getName()));

		// Execution
		assertEquals("Opaque behavior does not exist", "compute", interaction.getOwnedBehaviors().get(0)
				.getName());
		assertTrue(fragments.get(9) instanceof ExecutionOccurrenceSpecification);
		assertEquals("compute_finish2", fragments.get(9).getName());
		assertTrue(fragments.get(8) instanceof BehaviorExecutionSpecification);
		assertEquals("compute", fragments.get(8).getName());
		assertEquals(((BehaviorExecutionSpecification)fragments.get(8)).getStart(), fragments.get(7));
		assertEquals(((BehaviorExecutionSpecification)fragments.get(8)).getFinish(), fragments.get(9));
		assertNotNull(((BehaviorExecutionSpecification)fragments.get(8)).getCovered(source.getName()));
		assertEquals(interaction.getOwnedBehaviors().get(0),
				((BehaviorExecutionSpecification)fragments.get(8)).getBehavior());
	}

	/**
	 * Test createSynchronousMessage service. Create a synchronous message between an existing execution and
	 * an empty lifeline.
	 * 
	 * @throws Exception
	 *             in case of error
	 */
	public void testImportSynchronousMessageOnExecution() throws Exception {
		Interaction interaction = getInteraction(RESOURCE_URI2, "Scenario_3");
		Lifeline source = interaction.getLifeline("consumers");
		Lifeline target = interaction.getLifeline("producers");
		Operation operation = (Operation)target.getRepresents().getType().getOwnedElements().get(1);
		ExecutionSpecification execution = (ExecutionSpecification)interaction.getFragment("compute");

		sequenceServices.createSynchronousMessage(interaction, execution, target, operation,
				execution.getStart(), execution.getStart());

		List<InteractionFragment> fragments = interaction.getFragments();
		assertEquals(10, fragments.size());
		// Execution compute
		assertEquals("Opaque behavior does not exist", "compute", interaction.getOwnedBehaviors().get(0)
				.getName());
		assertTrue(fragments.get(0) instanceof ExecutionOccurrenceSpecification);
		assertEquals("compute_start", fragments.get(0).getName());
		assertTrue(fragments.get(5) instanceof ExecutionOccurrenceSpecification);
		assertEquals("compute_finish", fragments.get(5).getName());
		assertTrue(fragments.get(1) instanceof BehaviorExecutionSpecification);
		assertEquals("compute", fragments.get(1).getName());
		assertEquals(((BehaviorExecutionSpecification)fragments.get(1)).getStart(), fragments.get(0));
		assertEquals(((BehaviorExecutionSpecification)fragments.get(1)).getFinish(), fragments.get(5));
		assertNotNull(((BehaviorExecutionSpecification)fragments.get(1)).getCovered(source.getName()));
		assertEquals(interaction.getOwnedBehaviors().get(0),
				((BehaviorExecutionSpecification)fragments.get(1)).getBehavior());

		// Message get
		assertEquals("Message does not exist", "get", interaction.getMessages().get(0).getName());
		assertTrue(fragments.get(2) instanceof MessageOccurrenceSpecification);
		assertEquals("get_sender", fragments.get(2).getName());
		assertTrue(fragments.get(3) instanceof MessageOccurrenceSpecification);
		assertEquals("get_receiver", fragments.get(3).getName());
		assertEquals(((Message)interaction.getMessages().get(0)).getSendEvent(), fragments.get(2));
		assertEquals(((Message)interaction.getMessages().get(0)).getReceiveEvent(), fragments.get(3));
		assertNotNull(((MessageOccurrenceSpecification)fragments.get(2)).getCovered(source.getName()));
		assertNotNull(((MessageOccurrenceSpecification)fragments.get(3)).getCovered(target.getName()));

		// Execution get
		assertEquals("Opaque behavior does not exist", "get", interaction.getOwnedBehaviors().get(1)
				.getName());
		assertTrue(fragments.get(4) instanceof BehaviorExecutionSpecification);
		assertEquals("get", fragments.get(4).getName());
		assertEquals(((BehaviorExecutionSpecification)fragments.get(4)).getStart(), fragments.get(3));
		assertEquals(((BehaviorExecutionSpecification)fragments.get(4)).getFinish(), fragments.get(6));
		assertNotNull(((BehaviorExecutionSpecification)fragments.get(4)).getCovered(target.getName()));
		assertEquals(interaction.getOwnedBehaviors().get(1),
				((BehaviorExecutionSpecification)fragments.get(4)).getBehavior());

		// Reply message get
		assertEquals("Message does not exist", "get_reply", interaction.getMessages().get(1).getName());
		assertTrue(fragments.get(6) instanceof MessageOccurrenceSpecification);
		assertEquals("get_reply_sender", fragments.get(6).getName());
		assertTrue(fragments.get(7) instanceof MessageOccurrenceSpecification);
		assertEquals("get_reply_receiver", fragments.get(7).getName());
		assertEquals(((Message)interaction.getMessages().get(1)).getSendEvent(), fragments.get(6));
		assertEquals(((Message)interaction.getMessages().get(1)).getReceiveEvent(), fragments.get(7));
		assertNotNull(((MessageOccurrenceSpecification)fragments.get(6)).getCovered(target.getName()));
		assertNotNull(((MessageOccurrenceSpecification)fragments.get(7)).getCovered(source.getName()));

		assertTrue(fragments.get(9) instanceof ExecutionOccurrenceSpecification);
		assertEquals("compute_finish2", fragments.get(9).getName());

		// Execution compute split
		assertTrue(fragments.get(8) instanceof BehaviorExecutionSpecification);
		assertEquals("compute", fragments.get(8).getName());
		assertEquals(((BehaviorExecutionSpecification)fragments.get(8)).getStart(), fragments.get(7));
		assertEquals(((BehaviorExecutionSpecification)fragments.get(8)).getFinish(), fragments.get(9));
		assertNotNull(((BehaviorExecutionSpecification)fragments.get(8)).getCovered(source.getName()));
		assertEquals(interaction.getOwnedBehaviors().get(0),
				((BehaviorExecutionSpecification)fragments.get(8)).getBehavior());
	}

	/**
	 * Test createSynchronousMessage service. Import a synchronous message between to empty lifelines.
	 * 
	 * @throws Exception
	 *             in case of error
	 */
	public void testImportSynchronousMessage() throws Exception {
		Interaction interaction = getInteraction(RESOURCE_URI2, "Scenario_2");
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
}
