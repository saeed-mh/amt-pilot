import argparse
from pathlib import Path

from app.services.application_advisor import ApplicationAdvisor
from evals.dataset import load_dataset
from evals.scorer import build_report, evaluate_case, evaluate_error

DEFAULT_DATASET = Path(__file__).parent / "datasets" / "address_registration.json"


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(
        description="Evaluate the real AmtPilot AI advisor against synthetic cases."
    )
    parser.add_argument("--dataset", type=Path, default=DEFAULT_DATASET)
    parser.add_argument("--output", type=Path)
    parser.add_argument(
        "--fail-below",
        type=float,
        default=1.0,
        help="Exit with status 1 when the overall score is below this value.",
    )
    return parser.parse_args()


def main() -> int:
    args = parse_args()
    if not 0.0 <= args.fail_below <= 1.0:
        raise SystemExit("--fail-below must be between 0 and 1")

    dataset, cases = load_dataset(args.dataset)
    advisor = ApplicationAdvisor()
    results = []

    for case in cases:
        try:
            advice = advisor.advise(case.request)
            result = evaluate_case(case, advice)
        except Exception as error:  # noqa: BLE001 - each eval case must be reported
            result = evaluate_error(case, error)

        results.append(result)
        status = "PASS" if result.passed else "FAIL"
        print(f"[{status}] {case.id}: {result.score:.0%}")
        for check in result.checks:
            if not check.passed:
                print(f"  - {check.metric}: {check.name} ({check.details})")

    report = build_report(
        dataset_name=dataset.name,
        dataset_version=dataset.version,
        model=advisor.settings.google_model,
        results=results,
    )
    print(
        f"Overall: {report.overall_score:.0%} "
        f"({report.passed_cases}/{report.total_cases} cases passed)"
    )
    for metric, score in report.metrics.items():
        print(f"  {metric}: {score.score:.0%} ({score.passed}/{score.total})")

    if args.output:
        args.output.parent.mkdir(parents=True, exist_ok=True)
        args.output.write_text(
            report.model_dump_json(indent=2),
            encoding="utf-8",
        )
        print(f"Report written to {args.output}")

    return 0 if report.overall_score >= args.fail_below else 1


if __name__ == "__main__":
    raise SystemExit(main())
