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
package org.obeonetwork.dsl.uml2.design.services;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.uml2.uml.BehaviorExecutionSpecification;
import org.eclipse.uml2.uml.Element;
import org.eclipse.uml2.uml.ExecutionOccurrenceSpecification;
import org.eclipse.uml2.uml.ExecutionSpecification;
import org.eclipse.uml2.uml.Interaction;
import org.eclipse.uml2.uml.InteractionFragment;
import org.eclipse.uml2.uml.Lifeline;
import org.eclipse.uml2.uml.Message;
import org.eclipse.uml2.uml.MessageOccurrenceSpecification;
import org.eclipse.uml2.uml.MessageSort;
import org.eclipse.uml2.uml.NamedElement;
import org.eclipse.uml2.uml.OccurrenceSpecification;
import org.eclipse.uml2.uml.OpaqueBehavior;
import org.eclipse.uml2.uml.Operation;
import org.eclipse.uml2.uml.UMLFactory;

import fr.obeo.dsl.viewpoint.DDiagram;
import fr.obeo.dsl.viewpoint.DEdge;
import fr.obeo.dsl.viewpoint.DNode;

/**
 * Utility services to manage sequence diagrams.
 * 
 * @author Gonzague Reydet <a href="mailto:gonzague.reydet@obeo.fr">gonzague.reydet@obeo.fr</a>
 */
public class SequenceServices {
	/**
	 * Retrieves the context element ({@link Lifeline} or {@link ExecutionSpecification}) of the given
	 * {@link OccurrenceSpecification}.
	 * 
	 * @param occurrenceSpecification
	 *            the {@link OccurrenceSpecification} for which to find the context
	 * @return the {@link ExecutionSpecification} on which the given {@link OccurrenceSpecification} is
	 *         attached or otherwise, the {@link Lifeline}otherwise it is attached to.
	 */
	public NamedElement findOccurrenceSpecificationContext(OccurrenceSpecification occurrenceSpecification) {
		final Lifeline lifeline = occurrenceSpecification.getCovereds().get(0);
		Stack<NamedElement> context = new Stack<NamedElement>();
		context.add(lifeline);

		List<InteractionFragment> allFragments = occurrenceSpecification.getEnclosingInteraction()
				.getFragments();
		List<InteractionFragment> fragments = new ArrayList<InteractionFragment>();
		for (InteractionFragment fragment : allFragments) {
			if (fragment.getCovered(lifeline.getName()) != null)
				fragments.add(fragment);
		}

		for (int i = 0; i < fragments.size(); i++) {
			InteractionFragment e = fragments.get(i);
			InteractionFragment en;
			if (i + 1 < fragments.size())
				en = fragments.get(i + 1);
			else
				en = null;

			if (e instanceof MessageOccurrenceSpecification && en != null
					&& en instanceof ExecutionSpecification)
				context.add(en);

			if (e instanceof ExecutionOccurrenceSpecification) {
				if (en == null || !(en instanceof ExecutionSpecification))
					context.pop();
			}

			// Found our element
			if (e == occurrenceSpecification) {
				return context.peek();
			}

			if (e instanceof ExecutionOccurrenceSpecification) {
				if (en != null && en instanceof ExecutionSpecification)
					context.add(fragments.get(i + 1));
			}

			if (e instanceof MessageOccurrenceSpecification && isEnd(e, fragments)) {
				context.pop();
			}
		}

		return lifeline;
	}

	private boolean isEnd(InteractionFragment endCandidate, List<InteractionFragment> fragments) {
		List<InteractionFragment> executionFinishes = new ArrayList<InteractionFragment>();
		for (InteractionFragment fragment : fragments) {
			if (fragment instanceof BehaviorExecutionSpecification) {
				// Get start
				BehaviorExecutionSpecification behavior = (BehaviorExecutionSpecification)fragment;
				// Get finish
				executionFinishes.add(behavior.getFinish());
			}
		}
		return executionFinishes.contains(endCandidate);
	}

	private BehaviorExecutionSpecification getExecution(InteractionFragment occurence) {
		Map<InteractionFragment, BehaviorExecutionSpecification> behaviors = new HashMap<InteractionFragment, BehaviorExecutionSpecification>();
		for (InteractionFragment fragment : occurence.getEnclosingInteraction().getFragments()) {
			if (fragment instanceof BehaviorExecutionSpecification) {
				BehaviorExecutionSpecification behavior = (BehaviorExecutionSpecification)fragment;
				// Get start
				behaviors.put(behavior.getStart(), behavior);
				// Get finish
				behaviors.put(behavior.getFinish(), behavior);
			}
		}
		return behaviors.get(occurence);
	}

	/**
	 * Finds the first level of {@link ExecutionSpecification} in the context of the given {@link Lifeline}.
	 * 
	 * @param lifeline
	 *            the context.
	 * @return the {@link ExecutionSpecification} semantic candidates.
	 */
	public List<ExecutionSpecification> executionSemanticCandidates(Lifeline lifeline) {
		return getFirstLevelExecutions(lifeline, lifeline.getInteraction().getFragments());
	}

	/**
	 * Finds the first level of {@link ExecutionSpecification} in the context of the given
	 * {@link ExecutionSpecification}.
	 * 
	 * @param execution
	 *            the context.
	 * @return the {@link ExecutionSpecification} semantic candidates.
	 */
	public List<ExecutionSpecification> executionSemanticCandidates(ExecutionSpecification execution) {
		final List<InteractionFragment> fragments = execution.getEnclosingInteraction().getFragments();

		final int startIndex = fragments.indexOf(execution.getStart());
		final int finishIndex = fragments.indexOf(execution.getFinish());
		final List<InteractionFragment> candidateFragments = new ArrayList<InteractionFragment>(
				fragments.subList(startIndex + 2, finishIndex));
		return getFirstLevelExecutions(execution.getCovereds().get(0), candidateFragments);
	}

	/**
	 * Find the first level of {@link ExecutionSpecification} in the given {@link InteractionFragment} list.
	 * 
	 * @param lifeline
	 *            the {@link Lifeline} which is covered by the searched {@link ExecutionSpecification}
	 * @param candidateFragments
	 *            a sub-list of {@link InteractionFragment} to inspect for the first
	 *            {@link ExecutionSpecification} level.
	 * @return {@link List} of the {@link ExecutionSpecification}
	 */
	private List<ExecutionSpecification> getFirstLevelExecutions(Lifeline lifeline,
			final List<InteractionFragment> candidateFragments) {
		List<ExecutionSpecification> executions = new ArrayList<ExecutionSpecification>();
		ExecutionSpecification subExec = null;
		for (InteractionFragment fragment : candidateFragments) {
			if (fragment instanceof ExecutionSpecification && fragment.getCovereds().contains(lifeline)) {
				// Element on the same lifeline
				if (subExec == null) {
					subExec = (ExecutionSpecification)fragment;
				}
			} else if (fragment instanceof OccurrenceSpecification && subExec != null
					&& fragment.equals(subExec.getFinish())) {
				executions.add(subExec);
				subExec = null;
			}
		}

		return executions;
	}

	/**
	 * Delete the all the semantic elements attached to the given node. This will find the semantic elements
	 * of the current node and those coming recursively from sub-bordered nodes and linked edges.
	 * 
	 * @param node
	 *            the root node to delete.
	 * @return the parent diagram.
	 */
	public DDiagram fullDelete(DNode node) {
		final Collection<EObject> elementsToDelete = getSemanticElementsToDelete(node);

		for (EObject eObject : elementsToDelete) {
			EcoreUtil.remove(eObject);
		}

		return node.getParentDiagram();
	}

	/**
	 * Retrieves all the semantic elements of the current node, those of the incoming and outgoing edges and
	 * recursively along the sub-bordered node tree.
	 * 
	 * @param node
	 *            the root {@link DNode}
	 * @return the list of attached semantic elements
	 */
	private static Collection<EObject> getSemanticElementsToDelete(DNode node) {
		final Collection<EObject> elementsToDelete = new LinkedHashSet<EObject>();

		elementsToDelete.addAll(node.getSemanticElements());

		for (DEdge incomingEdge : node.getIncomingEdges()) {
			elementsToDelete.addAll(incomingEdge.getSemanticElements());
		}

		for (DEdge outgoingEdges : node.getOutgoingEdges()) {
			elementsToDelete.addAll(outgoingEdges.getSemanticElements());
		}

		for (DNode borderedNode : node.getOwnedBorderedNodes()) {
			elementsToDelete.addAll(getSemanticElementsToDelete(borderedNode));
		}

		return elementsToDelete;
	}

	/**
	 * Create a typed execution. Execution could be created on lifeline or other parent execution.
	 * 
	 * @param interaction
	 *            Interaction
	 * @param fragment
	 *            Lifeline or parent execution
	 * @param operation
	 *            Operation associated to execution
	 * @param startingEndPredecessor
	 *            Starting end predecessor
	 */
	public void createExecution(Interaction interaction, NamedElement fragment, Operation operation,
			NamedElement startingEndPredecessor) {
		Lifeline lifeline = getLifeline(fragment);

		UMLFactory factory = UMLFactory.eINSTANCE;
		StringBuffer executionName;
		if (operation == null) {
			List<BehaviorExecutionSpecification> behaviors = new ArrayList<BehaviorExecutionSpecification>();
			for (InteractionFragment behavior : interaction.getFragments()) {
				if (behavior instanceof BehaviorExecutionSpecification)
					behaviors.add((BehaviorExecutionSpecification)behavior);
			}
			executionName = new StringBuffer("BehaviorExecution_").append(behaviors.size());
		} else {
			executionName = new StringBuffer(operation.getName());
		}

		// Create execution start
		ExecutionOccurrenceSpecification startExec = factory.createExecutionOccurrenceSpecification();
		StringBuffer startExecName = new StringBuffer(executionName).append("_start");
		startExec.setName(startExecName.toString());
		startExec.getCovereds().add(lifeline);

		// Create behavior
		OpaqueBehavior behavior = factory.createOpaqueBehavior();
		behavior.setName(executionName.toString());
		behavior.setSpecification(operation);
		interaction.getOwnedBehaviors().add(behavior);
		BehaviorExecutionSpecification execution = factory.createBehaviorExecutionSpecification();
		execution.setName(executionName.toString());
		execution.getCovereds().add(lifeline);
		execution.setBehavior(behavior);

		execution.setStart(startExec);
		startExec.setExecution(execution);

		// Create execution end
		ExecutionOccurrenceSpecification endExec = factory.createExecutionOccurrenceSpecification();
		StringBuffer endExecName = new StringBuffer(executionName).append("_finish");
		endExec.setName(endExecName.toString());
		endExec.getCovereds().add(lifeline);
		endExec.setExecution(execution);
		execution.setFinish(endExec);

		// Add and order fragments under the interaction
		EList<InteractionFragment> fragments = interaction.getFragments();

		// Ordered fragments
		fragments.add(startExec);
		// If execution starts from an execution, add the new execution start after the execution
		// specification
		if (startingEndPredecessor instanceof OccurrenceSpecification
				&& getExecution((OccurrenceSpecification)startingEndPredecessor) != null
				&& startingEndPredecessor
						.equals(getExecution((OccurrenceSpecification)startingEndPredecessor).getStart()))
			fragments.move(fragments.indexOf(startingEndPredecessor) + 2, startExec);
		else
			// Message starts from a lifeline, add the message start after the last starting predecessor
			// (message)
			fragments.move(fragments.indexOf(startingEndPredecessor) + 1, startExec);
		fragments.add(execution);
		fragments.move(fragments.indexOf(startExec) + 1, execution);
		fragments.add(endExec);
		fragments.move(fragments.indexOf(execution) + 1, endExec);
	}

	/**
	 * Create a typed execution. Execution could be created on lifeline or other parent execution.
	 * 
	 * @param interaction
	 *            Interaction
	 * @param fragment
	 *            Lifeline or parent execution
	 * @param operation
	 *            Operation associated to execution
	 * @param startingEndPredecessor
	 *            Starting end predecessor
	 */
	public void createExecution(Interaction interaction, NamedElement fragment,
			NamedElement startingEndPredecessor) {
		createExecution(interaction, fragment, null, startingEndPredecessor);
	}

	/**
	 * Create asynchronous typed message.
	 * 
	 * @param interaction
	 *            Interaction
	 * @param sourceFragment
	 *            Source
	 * @param targetFragment
	 *            Target
	 * @param operation
	 *            Operation associated to message
	 * @param startingEndPredecessor
	 *            Starting end predecessor
	 * @param finishingEndPredecessor
	 *            Finishing end predecessor
	 */
	public void createAsynchronousMessage(Interaction interaction, NamedElement sourceFragment,
			NamedElement targetFragment, Operation operation, NamedElement startingEndPredecessor,
			NamedElement finishingEndPredecessor) {
		Lifeline source = getLifeline(sourceFragment);
		Lifeline target = getLifeline(targetFragment);

		UMLFactory factory = UMLFactory.eINSTANCE;

		// Create message
		Message message = factory.createMessage();
		StringBuffer operationName;
		if (operation == null && targetFragment instanceof Lifeline)
			operationName = new StringBuffer("Message_").append(interaction.getMessages().size());
		else if (targetFragment instanceof BehaviorExecutionSpecification) {
			// If message finishes on an execution, reference the operation associated to the execution
			operationName = new StringBuffer(targetFragment.getName());
		} else
			operationName = new StringBuffer(operation.getName());

		message.setName(operationName.toString());
		message.setMessageSort(MessageSort.SYNCH_CALL_LITERAL);
		interaction.getMessages().add(message);

		// Create message send event
		MessageOccurrenceSpecification senderEventMessage = factory.createMessageOccurrenceSpecification();
		StringBuffer senderEventName = new StringBuffer(operationName).append("_sender");
		senderEventMessage.setName(senderEventName.toString());
		senderEventMessage.getCovereds().add(source);
		senderEventMessage.setMessage(message);

		// Create message receive event
		MessageOccurrenceSpecification receiverEventMessage = factory.createMessageOccurrenceSpecification();
		StringBuffer receiverEventName = new StringBuffer(operationName).append("_receiver");
		receiverEventMessage.setName(receiverEventName.toString());
		receiverEventMessage.getCovereds().add(target);
		receiverEventMessage.setMessage(message);

		message.setSendEvent(senderEventMessage);
		message.setReceiveEvent(receiverEventMessage);

		// Add and order fragments under the interaction
		EList<InteractionFragment> fragments = interaction.getFragments();

		// Ordered fragments
		fragments.add(senderEventMessage);
		// If message starts from an execution, add the message start after the execution specification
		if (sourceFragment instanceof BehaviorExecutionSpecification)
			fragments.move(fragments.indexOf(sourceFragment) + 1, senderEventMessage);
		else
			// Message starts from a lifeline, add the message start after the last starting predecessor
			// (message)
			fragments.move(fragments.indexOf(startingEndPredecessor) + 1, senderEventMessage);
		interaction.getFragments().add(receiverEventMessage);

		// If message starts from an execution and is not typed, add the message end after the execution end
		if (operation == null && targetFragment instanceof Lifeline
				&& sourceFragment instanceof BehaviorExecutionSpecification)
			fragments.move(fragments.indexOf(((ExecutionSpecification)sourceFragment).getFinish()) + 1,
					receiverEventMessage);
		else if (targetFragment instanceof BehaviorExecutionSpecification) {
			// Delete execution start and set message receive as execution start
			fragments.move(fragments.indexOf(((BehaviorExecutionSpecification)targetFragment).getStart()),
					receiverEventMessage);
			fragments.remove(((BehaviorExecutionSpecification)targetFragment).getStart());
			((BehaviorExecutionSpecification)targetFragment).setStart(senderEventMessage);
		} else
			fragments.move(fragments.indexOf(senderEventMessage) + 1, receiverEventMessage);

		BehaviorExecutionSpecification execution = null;
		if (operation != null) {
			// Create behavior
			OpaqueBehavior behavior = factory.createOpaqueBehavior();
			behavior.setName(operationName.toString());
			if (operation != null || targetFragment instanceof ExecutionSpecification)
				behavior.setSpecification(operation);
			interaction.getOwnedBehaviors().add(behavior);

			execution = factory.createBehaviorExecutionSpecification();
			execution.setName(operationName.toString());
			execution.getCovereds().add(target);
			execution.setBehavior(behavior);
			execution.setStart(receiverEventMessage);
		} else if (targetFragment instanceof BehaviorExecutionSpecification) {
			execution = (BehaviorExecutionSpecification)targetFragment;
		}

		// Create execution end
		ExecutionOccurrenceSpecification endExec = factory.createExecutionOccurrenceSpecification();
		StringBuffer executionEndName = new StringBuffer(execution.getName()).append("_finish");
		endExec.setName(executionEndName.toString());
		endExec.getCovereds().add(target);
		endExec.setExecution(execution);
		if (execution != null && execution.getFinish() != null) {
			fragments.remove(execution.getFinish());
		}

		execution.setFinish(endExec);
		interaction.getFragments().add(execution);
		fragments.move(fragments.indexOf(receiverEventMessage) + 1, execution);
		fragments.add(endExec);
		// If message starts from an execution, add the message end after the parent execution end
		if (sourceFragment instanceof BehaviorExecutionSpecification)
			fragments.move(
					fragments.indexOf(((BehaviorExecutionSpecification)sourceFragment).getFinish()) + 1,
					endExec);
		else
			fragments.move(fragments.indexOf(execution) + 1, endExec);

	}

	/**
	 * Create asynchronous message.
	 * 
	 * @param interaction
	 *            Interaction
	 * @param sourceFragment
	 *            Source
	 * @param targetFragment
	 *            Target
	 * @param startingEndPredecessor
	 *            Starting end predecessor
	 * @param finishingEndPredecessor
	 *            Finishing end predecessor
	 */
	public void createAsynchronousMessage(Interaction interaction, NamedElement sourceFragment,
			NamedElement targetFragment, NamedElement startingEndPredecessor,
			NamedElement finishingEndPredecessor) {
		createAsynchronousMessage(interaction, sourceFragment, targetFragment, null, startingEndPredecessor,
				finishingEndPredecessor);
	}

	/**
	 * Create synchronous typed message.
	 * 
	 * @param interaction
	 *            Interaction
	 * @param sourceFragment
	 *            Source
	 * @param targetFragment
	 *            Target
	 * @param operation
	 *            Operation associated to message
	 * @param startingEndPredecessor
	 *            Starting end predecessor
	 * @param finishingEndPredecessor
	 *            Finishing end predecessor
	 */
	public void createSynchronousMessage(Interaction interaction, NamedElement sourceFragment,
			NamedElement targetFragment, Operation operation, NamedElement startingEndPredecessor,
			NamedElement finishingEndPredecessor) {
		Lifeline source = getLifeline(sourceFragment);
		Lifeline target = getLifeline(targetFragment);

		UMLFactory factory = UMLFactory.eINSTANCE;

		// Create message
		Message message = factory.createMessage();
		StringBuffer operationName;
		if (operation == null && targetFragment instanceof Lifeline)
			operationName = new StringBuffer("Message_").append(interaction.getMessages().size());
		else if (targetFragment instanceof BehaviorExecutionSpecification) {
			// If message finishes on an execution, reference the operation associated to the execution
			operationName = new StringBuffer(targetFragment.getName());
		} else
			operationName = new StringBuffer(operation.getName());

		message.setName(operationName.toString());
		message.setMessageSort(MessageSort.SYNCH_CALL_LITERAL);
		interaction.getMessages().add(message);

		// Create message send event
		MessageOccurrenceSpecification senderEventMessage = factory.createMessageOccurrenceSpecification();
		StringBuffer senderEventName = new StringBuffer(operationName).append("_sender");
		senderEventMessage.setName(senderEventName.toString());
		senderEventMessage.getCovereds().add(source);
		senderEventMessage.setMessage(message);

		// Create message receive event
		MessageOccurrenceSpecification receiverEventMessage = factory.createMessageOccurrenceSpecification();
		StringBuffer receiverEventName = new StringBuffer(operationName).append("_receiver");
		receiverEventMessage.setName(receiverEventName.toString());
		receiverEventMessage.getCovereds().add(target);
		receiverEventMessage.setMessage(message);

		message.setSendEvent(senderEventMessage);
		message.setReceiveEvent(receiverEventMessage);

		// Add and order fragments under the interaction
		EList<InteractionFragment> fragments = interaction.getFragments();

		// Ordered fragments
		fragments.add(senderEventMessage);
		// If message starts from an execution, add the message start after the execution specification
		if (sourceFragment instanceof BehaviorExecutionSpecification)
			fragments.move(fragments.indexOf(sourceFragment) + 1, senderEventMessage);
		else
			// Message starts from a lifeline, add the message start after the last starting predecessor
			// (message)
			fragments.move(fragments.indexOf(startingEndPredecessor) + 1, senderEventMessage);
		interaction.getFragments().add(receiverEventMessage);

		// If message starts from an execution and is not typed, add the message end after the execution end
		if (operation == null && targetFragment instanceof Lifeline
				&& sourceFragment instanceof BehaviorExecutionSpecification)
			fragments.move(fragments.indexOf(((ExecutionSpecification)sourceFragment).getFinish()) + 1,
					receiverEventMessage);
		else if (targetFragment instanceof BehaviorExecutionSpecification) {
			// Delete execution start and set message receive as execution start
			fragments.move(fragments.indexOf(((BehaviorExecutionSpecification)targetFragment).getStart()),
					receiverEventMessage);
			fragments.remove(((BehaviorExecutionSpecification)targetFragment).getStart());
			((BehaviorExecutionSpecification)targetFragment).setStart(senderEventMessage);
		} else
			fragments.move(fragments.indexOf(senderEventMessage) + 1, receiverEventMessage);

		// Create behavior
		OpaqueBehavior behavior = factory.createOpaqueBehavior();
		behavior.setName(operationName.toString());
		if (operation != null || targetFragment instanceof ExecutionSpecification)
			behavior.setSpecification(operation);
		interaction.getOwnedBehaviors().add(behavior);

		BehaviorExecutionSpecification execution = null;
		if (operation != null) {
			execution = factory.createBehaviorExecutionSpecification();
			execution.setName(operationName.toString());
			execution.getCovereds().add(target);
			execution.setBehavior(behavior);
			execution.setStart(receiverEventMessage);
		} else if (targetFragment instanceof BehaviorExecutionSpecification) {
			execution = (BehaviorExecutionSpecification)targetFragment;
		}

		// Create reply message
		Message replyMessage = factory.createMessage();
		StringBuffer replyName = new StringBuffer(operationName).append("_reply");
		replyMessage.setName(replyName.toString());
		replyMessage.setMessageSort(MessageSort.REPLY_LITERAL);
		interaction.getMessages().add(replyMessage);

		// Create reply message send event
		MessageOccurrenceSpecification senderEventReplyMessage = factory
				.createMessageOccurrenceSpecification();
		StringBuffer senderReplyEventName = new StringBuffer(replyName).append("_sender");
		senderEventReplyMessage.setName(senderReplyEventName.toString());
		senderEventReplyMessage.getCovereds().add(target);
		senderEventReplyMessage.setMessage(replyMessage);
		if (execution != null && execution.getFinish() != null) {
			fragments.remove(execution.getFinish());
		}

		// Create reply message receive event
		MessageOccurrenceSpecification receiverEventReplyMessage = factory
				.createMessageOccurrenceSpecification();
		StringBuffer receiverReplyEventName = new StringBuffer(replyName).append("_receiver");
		receiverEventReplyMessage.setName(receiverReplyEventName.toString());
		receiverEventReplyMessage.getCovereds().add(source);
		receiverEventReplyMessage.setMessage(replyMessage);

		replyMessage.setSendEvent(senderEventReplyMessage);
		replyMessage.setReceiveEvent(receiverEventReplyMessage);

		if (execution != null) {
			execution.setFinish(senderEventReplyMessage);
			interaction.getFragments().add(execution);
			fragments.move(fragments.indexOf(receiverEventMessage) + 1, execution);
			fragments.add(senderEventReplyMessage);
			fragments.move(fragments.indexOf(execution) + 1, senderEventReplyMessage);
			fragments.add(receiverEventReplyMessage);
			fragments.move(fragments.indexOf(senderEventReplyMessage) + 1, receiverEventReplyMessage);
		} else {
			fragments.add(senderEventReplyMessage);
			fragments.move(fragments.indexOf(receiverEventMessage) + 1, senderEventReplyMessage);
			fragments.add(receiverEventReplyMessage);
			fragments.move(fragments.indexOf(senderEventReplyMessage) + 1, receiverEventReplyMessage);
		}

		if (sourceFragment instanceof BehaviorExecutionSpecification) {
			// Split source execution
			BehaviorExecutionSpecification splitSourceExec = factory.createBehaviorExecutionSpecification();
			splitSourceExec.setName(sourceFragment.getName());
			splitSourceExec.getCovereds().add(source);
			splitSourceExec.setBehavior(((BehaviorExecutionSpecification)sourceFragment).getBehavior());

			// Get source execution end
			OccurrenceSpecification endSourceExec = ((BehaviorExecutionSpecification)sourceFragment)
					.getFinish();

			// Create split execution end occurrence
			ExecutionOccurrenceSpecification endSplitSourceExec = factory
					.createExecutionOccurrenceSpecification();
			endSplitSourceExec.setName(sourceFragment.getName() + "_finish2");

			// Set split source execution start to reply message receiver
			splitSourceExec.setStart(receiverEventReplyMessage);
			splitSourceExec.setFinish(endSplitSourceExec);

			// Move execution source end after the created execution
			if (execution != null)
				fragments.move(fragments.indexOf(execution) + 1, endSourceExec);

			fragments.add(splitSourceExec);
			fragments.move(fragments.indexOf(receiverEventReplyMessage) + 1, splitSourceExec);
			fragments.add(endSplitSourceExec);
			fragments.move(fragments.indexOf(splitSourceExec) + 1, endSplitSourceExec);
		}
	}

	/**
	 * Create synchronous typed message.
	 * 
	 * @param interaction
	 *            Interaction
	 * @param sourceFragment
	 *            Source
	 * @param targetFragment
	 *            Target
	 * @param operation
	 *            Operation associated to message
	 * @param startingEndPredecessor
	 *            Starting end predecessor
	 * @param finishingEndPredecessor
	 *            Finishing end predecessor
	 */
	public void createSynchronousMessage(Interaction interaction, NamedElement sourceFragment,
			NamedElement targetFragment, NamedElement startingEndPredecessor,
			NamedElement finishingEndPredecessor) {
		createSynchronousMessage(interaction, sourceFragment, targetFragment, null, startingEndPredecessor,
				finishingEndPredecessor);
	}

	/**
	 * Delete execution.
	 * 
	 * @param execution
	 *            Execution to delete
	 */
	public void delete(BehaviorExecutionSpecification execution) {
		// Get fragments
		Interaction interaction = (Interaction)execution.eContainer();

		// Delete opaque behavior
		interaction.getOwnedBehaviors().remove(execution.getBehavior());
		// Delete start and finish behavior
		List<InteractionFragment> fragments = interaction.getFragments();
		if (execution.getStart() instanceof ExecutionOccurrenceSpecification)
			fragments.remove(execution.getStart());
		if (execution.getFinish() instanceof ExecutionOccurrenceSpecification)
			fragments.remove(execution.getFinish());
		// Delete execution
		fragments.remove(execution);
	}

	/**
	 * Delete message.
	 * 
	 * @param message
	 *            Message to delete
	 */
	public void delete(Message message) {
		// Get fragments
		Interaction interaction = (Interaction)message.eContainer();
		List<InteractionFragment> fragments = interaction.getFragments();

		// Delete start and finish message and if an execution is associated to the message remove also the
		// execution
		InteractionFragment receiveEvent = (InteractionFragment)message.getReceiveEvent();
		// If message is a synchronous message delete also the reply message
		if (MessageSort.SYNCH_CALL_LITERAL.equals(message.getMessageSort())) {
			if (getExecution(receiveEvent) != null
					&& getExecution(receiveEvent).getFinish() instanceof MessageOccurrenceSpecification)
				delete(((MessageOccurrenceSpecification)getExecution(receiveEvent).getFinish()).getMessage());
		}

		if (getExecution(receiveEvent) != null)
			delete(getExecution(receiveEvent));
		fragments.remove(receiveEvent);

		InteractionFragment sendEvent = (InteractionFragment)message.getSendEvent();
		if (getExecution(sendEvent) != null)
			delete(getExecution(sendEvent));
		fragments.remove(message.getSendEvent());

		// Delete message
		interaction.getMessages().remove(message);
	}

	private Lifeline getLifeline(Element fragment) {
		if (fragment instanceof Lifeline) {
			return (Lifeline)fragment;
		} else if (fragment instanceof ExecutionSpecification) {
			List<Lifeline> lifelines = ((ExecutionSpecification)fragment).getCovereds();
			if (lifelines != null && !lifelines.isEmpty()) {
				return lifelines.get(0);
			}
		}
		return null;
	}
}
