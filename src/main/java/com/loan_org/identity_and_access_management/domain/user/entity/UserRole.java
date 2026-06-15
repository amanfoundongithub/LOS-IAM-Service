package com.loan_org.identity_and_access_management.domain.user.entity;

public enum UserRole {

    /**
     * System Administrator.
     * Manages employee IAM provisioning, resets internal passwords, and overrides system configurations.
     * Crucial Security Constraint: Cannot create or approve loan applications to prevent internal fraud.
     */
    ADMIN,

    /**
     * Relationship Manager / Loan Officer.
     * The front-facing asset generator who sources deals, inputs borrower profiles,
     * uploads applicant documentation, and moves draft loan applications into the pipeline.
     */
    RELATIONSHIP_MANAGER,

    /**
     * Credit Underwriter.
     * The initial financial gatekeeper who reviews financial statements, verifies Debt-to-Income (DTI)
     * ratios, assesses risk parameters, and issues approvals/rejections within strict individual signing caps.
     */
    UNDERWRITER,

    /**
     * Senior Credit Manager.
     * Serves as the escalation tier for complex, high-value commercial deals or applications
     * that exceed standard underwriting limits. Possesses elevated authority to grant policy exceptions.
     */
    CREDIT_MANAGER,

    /**
     * Compliance & Fraud Agent.
     * Dedicated operational risk officer who reviews Know-Your-Customer (KYC) documents,
     * evaluates Anti-Money Laundering (AML) alerts, and checks global sanction/blacklist databases.
     */
    COMPLIANCE_AGENT,

    /**
     * Legal & Collateral Appraiser.
     * Specialized legal or asset assessment professional who validates property titles,
     * reviews hardware/physical collateral evaluations, and executes corporate lien registrations.
     */
    COLLATERAL_EVALUATOR,

    /**
     * Loan Closing Officer.
     * Coordinates the execution of final binding loan agreements, validates interest lock periods,
     * checks for outstanding closing conditions, and generates notarized digital signing packages.
     */
    CLOSING_OFFICER,

    /**
     * Financial Disbursement Clerk.
     * Controls the actual cash outflux pipelines. Triggers localized bank wire clearings or ACH
     * payment rail integration webhooks once a loan has reached an absolute "CLOSED_APPROVED" state.
     */
    DISBURSEMENT_OFFICER,

    /**
     * Internal Quality Assurance Auditor.
     * Post-disbursement reviewer who samples randomly selected loan files to ensure corporate lending
     * policies, federal regulations, and underwriting standards were met perfectly. Read-only permissions.
     */
    QUALITY_AUDITOR,

    /**
     * Customer Service Representative.
     * Front-line support staff capable of viewing high-level active loan pipeline states and
     * servicing schedules to assist existing borrowers with payment processing or inquiry updates.
     */
    CUSTOMER_SUPPORT,

    /**
     * Collection & Recovery Specialist.
     * Back-office operations risk agent assigned to manage delinquent user accounts, coordinate
     * restructuring workflows, issue formal default warnings, or execute collateral liquidations.
     */
    RECOVERY_SPECIALIST
}